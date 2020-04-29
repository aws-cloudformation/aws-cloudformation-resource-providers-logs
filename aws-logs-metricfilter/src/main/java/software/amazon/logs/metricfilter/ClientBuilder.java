package software.amazon.logs.metricfilter;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

public class ClientBuilder {
    public static CloudWatchLogsClient getClient() {
        return CloudWatchLogsClient.builder()
            .build();
    }
}
