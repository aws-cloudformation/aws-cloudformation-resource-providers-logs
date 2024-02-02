package software.amazon.logs.metricfilter;

import software.amazon.logs.common.MetricsProvider;

class Configuration extends BaseConfiguration {
    static {
        MetricsProvider.initialize(ResourceModel.TYPE_NAME);
    }

    public Configuration() {
        super("aws-logs-metricfilter.json");
    }
}
