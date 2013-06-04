package com.rallydev.intellij.wsapi

class ConnectionTest {

    URL server
    RallyClient rallyClient

    ConnectionTest(RallyClient rallyClient) {
        this.server = server
        this.rallyClient = rallyClient
    }

    void doTest() throws Exception {
        ApiResponse response = rallyClient.makeRequest(new GetRequest(ApiEndpoint.WORKSPACE))
        if (!response?.results) {
            throw new RuntimeException("Incorrect response from server\n${response ?: 'No response'}")
        }
    }

}
