package software.amazon.logs.subscriptionfilter;

import java.time.Duration;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.internal.retry.SdkDefaultRetrySetting;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.core.retry.backoff.BackoffStrategy;
import software.amazon.awssdk.core.retry.backoff.EqualJitterBackoffStrategy;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.AbstractWrapper;

public class ClientBuilder {

    private static CloudWatchLogsClient cloudWatchLogsClient;

    private static final BackoffStrategy BACKOFF_STRATEGY = EqualJitterBackoffStrategy
        .builder()
        .baseDelay(Duration.ofSeconds(2))
        .maxBackoffTime(SdkDefaultRetrySetting.MAX_BACKOFF)
        .build();

    private static final RetryPolicy RETRY_POLICY = RetryPolicy
        .builder()
        .numRetries(4)
        .retryCondition(RetryCondition.defaultRetryCondition())
        .throttlingBackoffStrategy(BACKOFF_STRATEGY)
        .build();

    public static CloudWatchLogsClient getClient() {
        if (cloudWatchLogsClient == null) {
            cloudWatchLogsClient =
                CloudWatchLogsClient
                    .builder()
                    .httpClient(AbstractWrapper.HTTP_CLIENT)
                    .overrideConfiguration(
                        ClientOverrideConfiguration.builder().retryPolicy(RETRY_POLICY).apiCallTimeout(Duration.ofSeconds(55)).build()
                    )
                    .build();
            return cloudWatchLogsClient;
        }
        return cloudWatchLogsClient;
    }
}
