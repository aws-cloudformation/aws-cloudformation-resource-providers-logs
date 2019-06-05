package com.aws.logs.metricfilter;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;

public class ClientBuilder {
    public static CloudWatchClient getClient() {
        return CloudWatchClient.builder()
            .overrideConfiguration(ClientOverrideConfiguration.builder()
                .retryPolicy(RetryPolicy.builder().numRetries(16).build())
                .build())
            .build();
    }
}
