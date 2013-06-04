package com.rallydev.intellij.wsapi

import spock.lang.Specification

class GetRequestSpec extends Specification {

    def "Simple object, no filter"() {
        given:
        String rallyUri = 'https://rally1.rallydev.com/'
        GetRequest wsapiRequest = new GetRequest(ApiEndpoint.WORKSPACE)

        expect:
        wsapiRequest.getUrl(rallyUri.toURL()) == "${rallyUri}/slm/webservice/${GetRequest.WSAPI_VERSION}/workspace.js"
    }

    def "Simple object with single query param"() {
        given:
        String rallyUri = 'https://rally1.rallydev.com/'
        GetRequest wsapiRequest = new GetRequest(ApiEndpoint.WORKSPACE)
                .withFetch()

        expect:
        wsapiRequest.getUrl(rallyUri.toURL()) == "${rallyUri}/slm/webservice/${GetRequest.WSAPI_VERSION}/workspace.js?fetch=true"
    }

    def "With objectId"() {
        given:
        String rallyUri = 'https://rally1.rallydev.com/'
        GetRequest wsapiRequest = new GetRequest(ApiEndpoint.WORKSPACE)
                .withFetch()
                .withObjectId('5')

        expect:
        wsapiRequest.getUrl(rallyUri.toURL()) == "${rallyUri}/slm/webservice/${GetRequest.WSAPI_VERSION}/workspace/5.js?fetch=true"
    }

    def "With query"() {
        given:
        String rallyUri = 'https://rally1.rallydev.com/'
        GetRequest wsapiRequest = new GetRequest(ApiEndpoint.WORKSPACE)
                .withQuery('(Name contains "Matt")')

        expect:
        wsapiRequest.getUrl(rallyUri.toURL()) == "${rallyUri}/slm/webservice/${GetRequest.WSAPI_VERSION}/workspace.js?query=(Name contains \"Matt\")"
    }

    def "Encoded query"() {
        given:
        String rallyUri = 'https://rally1.rallydev.com/'
        GetRequest wsapiRequest = new GetRequest(ApiEndpoint.WORKSPACE)
                .withQuery('(Name contains "Matt")')

        expect:
        wsapiRequest.getEncodedUrl(rallyUri.toURL()) == "${rallyUri}/slm/webservice/${GetRequest.WSAPI_VERSION}/workspace.js?query=(Name%20contains%20%22Matt%22)"
    }

    def "With fetch"() {
        given:
        String rallyUri = 'https://rally1.rallydev.com/'
        GetRequest wsapiRequest = new GetRequest(ApiEndpoint.WORKSPACE).withFetch()

        expect:
        wsapiRequest.getUrl(rallyUri.toURL()) == "${rallyUri}/slm/webservice/${GetRequest.WSAPI_VERSION}/workspace.js?fetch=true"
    }

    def "With pagesize"() {
        given:
        String rallyUri = 'https://rally1.rallydev.com/'
        GetRequest wsapiRequest

        when:
        wsapiRequest = new GetRequest(ApiEndpoint.WORKSPACE).withPageSize(5)

        then:
        wsapiRequest.getUrl(rallyUri.toURL()) == "${rallyUri}/slm/webservice/${GetRequest.WSAPI_VERSION}/workspace.js?pagesize=5"

        when:
        wsapiRequest = new GetRequest(ApiEndpoint.WORKSPACE).withPageSize(-3)

        then:
        wsapiRequest.getUrl(rallyUri.toURL()) == "${rallyUri}/slm/webservice/${GetRequest.WSAPI_VERSION}/workspace.js?pagesize=${GetRequest.MIN_PAGE_SIZE}"

        when:
        wsapiRequest = new GetRequest(ApiEndpoint.WORKSPACE).withPageSize(200)

        then:
        wsapiRequest.getUrl(rallyUri.toURL()) == "${rallyUri}/slm/webservice/${GetRequest.WSAPI_VERSION}/workspace.js?pagesize=${GetRequest.MAX_PAGE_SIZE}"
    }

}
