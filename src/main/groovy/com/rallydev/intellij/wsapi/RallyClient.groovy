package com.rallydev.intellij.wsapi

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.ListenableFuture
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.rallydev.intellij.config.PasswordNotConfiguredException
import com.rallydev.intellij.config.RallyConfig
import com.rallydev.intellij.config.RallyPasswordDialog
import com.rallydev.intellij.util.AsyncService
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpStatus
import org.apache.commons.httpclient.UsernamePasswordCredentials
import org.apache.commons.httpclient.auth.AuthScope
import org.apache.commons.httpclient.auth.InvalidCredentialsException
import org.apache.commons.httpclient.methods.GetMethod
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class RallyClient extends HttpClient {
    private static final Logger log = Logger.getInstance(RallyClient)

    AsyncService asyncService

    RallyClient(AsyncService asyncService) {
        this.asyncService = asyncService
    }

    public static RallyClient getInstance() {
        return ServiceManager.getService(RallyClient.class)
    }

    ListenableFuture<ApiResponse> makeRequest(@NotNull GetRequest request, @Nullable FutureCallback<ApiResponse> callback = null) {
        ensurePasswordLoaded()
        Closure<ApiResponse> requestCall = {
            state.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(getUsername(), getPassword()))

            GetMethod method = buildMethod(request)
            log.debug "Rally Client requesting [${method.URI}]"
            int code = executeMethod(method)

            switch (code) {
                case HttpStatus.SC_OK:
                    return new ApiResponse(method.responseBodyAsString)
                case HttpStatus.SC_UNAUTHORIZED:
                    onAuthError()
                    throw new InvalidCredentialsException('The provided user name and password are not valid')
                default:
                    throw new RuntimeException('Unhandled response code')
            }
        }

        return asyncService.schedule(requestCall, callback)
    }

    protected GetMethod buildMethod(GetRequest request) {
        GetMethod method = new GetMethod(request.getEncodedUrl(getServer()))
        method.addRequestHeader('X-RallyIntegrationName', 'IntelliJ Plugin')
        method.addRequestHeader('X-RallyIntegrationVendor', 'Rally Software')
        method.addRequestHeader('X-RallyIntegrationPlatform', "${ApplicationInfo.instance?.build}")

        method
    }

    protected String promptForPassword() {
        if (ApplicationManager.getApplication().isDispatchThread()) {
            return RallyPasswordDialog.askPassword()
        }
        null
    }

    protected void ensurePasswordLoaded() {
        String password = getPassword()
        if (!password) {
            password = promptForPassword()
            if (password) {
                RallyConfig.instance.password = password
            }
        }

        if (!password) {
            throw new PasswordNotConfiguredException()
        }
    }

    protected void onAuthError() {
        RallyConfig.instance.clearCachedPassword()
    }

    URL getServer() {
        return RallyConfig.instance.url.toURL()
    }

    String getUsername() {
        return RallyConfig.instance.userName
    }

    String getPassword() {
        return RallyConfig.instance.password
    }

}
