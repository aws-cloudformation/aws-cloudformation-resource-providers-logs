package software.amazon.logs.querydefinition;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.LambdaWrapper;

final class ClientBuilder {

    static CloudWatchLogsClient getLogsClient() {
        return CloudWatchLogsClient.builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .build();
    }
}
