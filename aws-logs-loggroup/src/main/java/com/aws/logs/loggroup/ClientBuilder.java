package com.aws.logs.loggroup;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

public class ClientBuilder {
    public static CloudWatchLogsClient getClient() {
        return CloudWatchLogsClient.builder()
                .build();
    }
}
