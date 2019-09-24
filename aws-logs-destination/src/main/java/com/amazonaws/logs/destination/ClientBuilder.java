package com.amazonaws.logs.destination;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

public class ClientBuilder {
    public static CloudWatchLogsClient getClient() {
        return CloudWatchLogsClient.builder()
            .build();
    }
}
