package software.amazon.logs.metricfilter;

import static software.amazon.logs.common.MetricsHelper.putProperty;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;

/**
 * Helper class specific to Metric Filter resource to be used with an EMF MetricsLogger object to publish metrics and properties
 */
@Slf4j
final class MetricsHelper {

    public static final String LOG_GROUP_NAME = "LogGroupName";
    public static final String FILTER_NAME = "FilterName";
    public static final String FILTER_NAME_PREFIX = "FilterNamePrefix";

    private MetricsHelper() {}

    /**
     * Helper method to publish properties of a PutMetricFilterRequest. Usually, after our handler converts/translate a CFN request to a CWL request
     *
     * @param metrics    EMF MetricsLogger object
     * @param awsRequest CWL request object (after being translated from a CFN request to service request)
     */
    static void putMetricFilterRequestMetrics(final MetricsLogger metrics, final PutMetricFilterRequest awsRequest) {
        if (Objects.isNull(awsRequest)) return;

        putProperty(metrics, FILTER_NAME, awsRequest.filterName());
        putProperty(metrics, LOG_GROUP_NAME, awsRequest.logGroupName());
    }

    /**
     * Helper method to publish properties of a DescribeMetricFiltersRequest. Usually, after our handler converts/translate a CFN request to a CWL request
     *
     * @param metrics    EMF MetricsLogger object
     * @param awsRequest DescribeMetricFiltersRequest object (after being translated from a CFN request to service request)
     */
    static void putMetricFilterRequestMetrics(final MetricsLogger metrics, final DescribeMetricFiltersRequest awsRequest) {
        if (Objects.isNull(awsRequest)) return;

        putProperty(metrics, FILTER_NAME_PREFIX, awsRequest.filterNamePrefix());
        putProperty(metrics, LOG_GROUP_NAME, awsRequest.logGroupName());
    }

    /**
     * Helper method to publish properties of a DeleteMetricFilterRequest. Usually, after our handler converts/translate a CFN request to a CWL request
     *
     * @param metrics    EMF MetricsLogger object
     * @param awsRequest DeleteMetricFilterRequest object (after being translated from a CFN request to service request)
     */
    static void putMetricFilterRequestMetrics(final MetricsLogger metrics, final DeleteMetricFilterRequest awsRequest) {
        if (Objects.isNull(awsRequest)) return;

        putProperty(metrics, FILTER_NAME, awsRequest.filterName());
        putProperty(metrics, LOG_GROUP_NAME, awsRequest.logGroupName());
    }
}
