package software.amazon.logs.subscriptionfilter;

import software.amazon.logs.common.MetricsProvider;

class Configuration extends BaseConfiguration {
    static {
        MetricsProvider.initialize(ResourceModel.TYPE_NAME);
    }

    public Configuration() {
        super("aws-logs-subscriptionfilter.json");
    }
}
