package software.amazon.logs.loganomalydetector;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.internal.retry.SdkDefaultRetrySetting;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.core.retry.backoff.BackoffStrategy;
import software.amazon.awssdk.core.retry.backoff.EqualJitterBackoffStrategy;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;
import software.amazon.cloudformation.LambdaWrapper;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

import java.time.Duration;

import software.amazon.awssdk.regions.Region;


public class ClientBuilder {

    private static final Duration BASE_DELAY = Duration.ofSeconds(2);
    private static final Duration API_CALL_TIMEOUT = Duration.ofSeconds(55);
    private static final int MAX_RETRIES = 4;

    private ClientBuilder() {
    }

    private static final BackoffStrategy BACKOFF_STRATEGY =
            EqualJitterBackoffStrategy.builder()
                    .baseDelay(BASE_DELAY)
                    .maxBackoffTime(SdkDefaultRetrySetting.MAX_BACKOFF)
                    .build();

    private static final RetryPolicy RETRY_POLICY =
            RetryPolicy.builder()
                    .numRetries(MAX_RETRIES)
                    .retryCondition(RetryCondition.defaultRetryCondition())
                    .throttlingBackoffStrategy(BACKOFF_STRATEGY)
                    .build();

    public static CloudWatchLogsClient getLogsClient(final String region) {
        return CloudWatchLogsClient.builder()
                .region(Region.of(region))
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .retryPolicy(RETRY_POLICY)
                        .apiCallTimeout(API_CALL_TIMEOUT)
                        .build())
                .build();
    }

    public static CloudWatchLogsClient getLogsClient() {
        return CloudWatchLogsClient.builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .retryPolicy(RETRY_POLICY)
                        .apiCallTimeout(API_CALL_TIMEOUT)
                        .build())
                .build();
    }
}