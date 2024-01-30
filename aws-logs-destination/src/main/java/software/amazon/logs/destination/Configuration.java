package software.amazon.logs.destination;

import software.amazon.logs.common.MetricsProvider;

class Configuration extends BaseConfiguration {
    static {
        MetricsProvider.initialize(ResourceModel.TYPE_NAME);
    }

    public Configuration() {
        super("aws-logs-destination.json");
    }
}
