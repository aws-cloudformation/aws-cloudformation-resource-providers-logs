package com.amazonaws.logs.logstream;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

public class ClientBuilder {
    public static CloudWatchLogsClient getClient() {
        return CloudWatchLogsClient.builder()
            .overrideConfiguration(ClientOverrideConfiguration.builder()
                .retryPolicy(RetryPolicy.builder().numRetries(16).build())
                .build())
            .build();
    }
}
