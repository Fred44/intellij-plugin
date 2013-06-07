package com.rallydev.intellij.task;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.tasks.Task;
import com.intellij.tasks.impl.BaseRepository;
import com.intellij.tasks.impl.BaseRepositoryImpl;
import com.intellij.util.xmlb.annotations.Tag;
import com.rallydev.intellij.config.RallyConfig;
import com.rallydev.intellij.wsapi.ConnectionTest;
import com.rallydev.intellij.wsapi.RallyClient;
import org.jetbrains.annotations.Nullable;

import java.net.MalformedURLException;
import java.util.Collection;

@Tag("Rally")
public class RallyRepository extends BaseRepositoryImpl {
    private static final Logger log = Logger.getInstance(RallyRepository.class);

    public String workspaceId;
    public String testField;

    @SuppressWarnings("unused")
    public RallyRepository() {
    }

    public RallyRepository(RallyRepository other) {
        super(other);
        this.testField = other.testField;
        this.workspaceId = other.workspaceId;
    }

    public RallyRepository(RallyRepositoryType type) {
        super(type);
    }

    @Override
    public BaseRepository clone() {
        return new RallyRepository(this);
    }

    @Override
    public Task[] getIssues(@Nullable String query, int max, long since) throws Exception {
        Collection<RallyTask> rallyTasks = new WsapiQuery(getClient()).findTasks(query, max, since);
        return rallyTasks.toArray(new RallyTask[rallyTasks.size()]);
    }

    @Nullable
    @Override
    public Task findTask(String id) throws Exception {
        log.info("RallyRepository.findTask(String) invoked.");
        return new WsapiQuery(getClient()).findTask(id);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void testConnection() throws Exception {
        new ConnectionTest().doTest();
    }

    public RallyClient getClient() throws MalformedURLException {
        return ServiceManager.getService(RallyClient.class);
    }

    //Url is used in the server list, overriding to return a display name instead.
    @Override
    public String getUrl() {
        return RallyConfig.getInstance().url + " (" + workspaceId + ")";
    }

}
