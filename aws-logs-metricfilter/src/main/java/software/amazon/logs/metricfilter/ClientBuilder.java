package software.amazon.logs.metricfilter;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.internal.retry.SdkDefaultRetrySetting;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.core.retry.backoff.FullJitterBackoffStrategy;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.LambdaWrapper;

import java.time.Duration;

public class ClientBuilder {

  /**
   * Control plane APIs have a burst limit of 10 and rate limit of 5
   * This returns a client with a full jitter exponential back off strategy with
   * base delay of 2 seconds, to allow bucket of refilling to 10. Max back off time is 20 seconds, there's no point
   * of backing off for more than 20 seconds.
   *
   * @return a CloudWatchLogsClient
   */
  public static CloudWatchLogsClient getClient() {
    final Duration baseDelay = Duration.ofSeconds(2);
    final int maxRetries =  5_000;

    FullJitterBackoffStrategy backoffStrategy = FullJitterBackoffStrategy.builder()
            .baseDelay(baseDelay)
            .maxBackoffTime(SdkDefaultRetrySetting.MAX_BACKOFF)
            .build();

    final RetryPolicy retryPolicy = RetryPolicy.builder()
            .numRetries(maxRetries)
            .retryCondition(RetryCondition.defaultRetryCondition())
            .throttlingBackoffStrategy(backoffStrategy)
            .backoffStrategy(backoffStrategy)
            .build();

    ClientOverrideConfiguration configuration = ClientOverrideConfiguration.builder()
            .retryPolicy(retryPolicy)
            .build();

    return CloudWatchLogsClient.builder()
            .overrideConfiguration(configuration)
            .httpClient(LambdaWrapper.HTTP_CLIENT)
            .build();
  }
}
