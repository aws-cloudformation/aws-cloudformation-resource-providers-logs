package com.amazonaws.logs.logstream;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

public class ClientBuilder {
    public static CloudWatchLogsClient getClient() {
        return CloudWatchLogsClient.builder()
            .build();
    }
}
