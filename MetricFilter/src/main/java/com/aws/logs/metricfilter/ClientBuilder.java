package com.aws.logs.metricfilter;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClientBuilder;

import java.net.URI;

public class ClientBuilder {

    public static final CloudWatchLogsClient getCloudWatchLogsClient() {
        final CloudWatchLogsClientBuilder builder = CloudWatchLogsClient.builder()
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .retryPolicy(RetryPolicy.builder().numRetries(16).build())
                        .build());
        return builder.build();
    }

    public static final CloudWatchLogsClient getCloudWatchLogsClient(final String region, final String endpoint) {
        final CloudWatchLogsClientBuilder builder = CloudWatchLogsClient.builder()
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .retryPolicy(RetryPolicy.builder().numRetries(16).build())
                        .build());
        if (StringUtils.isNotEmpty(region)) {
            builder.region(Region.of(region));
        }
        if (StringUtils.isNotEmpty(endpoint)) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }
}
