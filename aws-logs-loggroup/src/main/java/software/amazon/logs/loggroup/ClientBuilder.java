package software.amazon.logs.loggroup;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.core.retry.conditions.RetryCondition;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.LambdaWrapper;

public class ClientBuilder {
    private ClientBuilder() {}

    private static final RetryPolicy RETRY_POLICY =
        RetryPolicy.builder()
            .numRetries(6)
            .retryCondition(RetryCondition.defaultRetryCondition())
            .build();

    public static CloudWatchLogsClient getClient() {
        return CloudWatchLogsClient.builder()
            .httpClient(LambdaWrapper.HTTP_CLIENT)
            .overrideConfiguration(ClientOverrideConfiguration.builder().retryPolicy(RETRY_POLICY).build())
            .build();
    }
}
