package software.amazon.logs.subscriptionfilter;

import static software.amazon.logs.common.MetricsHelper.putProperty;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteSubscriptionFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutSubscriptionFilterRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;

/**
 * Helper class specific to SubscriptionFilter resource to be used with an EMF MetricsLogger object to publish metrics and properties
 */
@Slf4j
public class MetricsHelper {

    public static final String FILTER_NAME = "FilterName";
    public static final String FILTER_NAME_PREFIX = "FilterNamePrefix";
    public static final String LOG_GROUP_NAME = "LogGroupName";

    private MetricsHelper() {}

    /**
     * Helper method to publish properties of a PutSubscriptionFilterRequest. Usually, after our handler converts/translates a CFN request to a CWL request
     *
     * @param metrics    EMF MetricsLogger object
     * @param awsRequest PutSubscriptionFilterRequest object (after being translated from a CFN request to service request)
     */
    static void putSubscriptionFilterRequestMetrics(final MetricsLogger metrics, final PutSubscriptionFilterRequest awsRequest) {
        if (Objects.isNull(awsRequest)) return;

        putProperty(metrics, FILTER_NAME, awsRequest.filterName());
        putProperty(metrics, LOG_GROUP_NAME, awsRequest.logGroupName());
    }

    /**
     * Helper method to publish properties of a DescribeSubscriptionFilterRequest. Usually, after our handler converts/translates a CFN request to a CWL request
     *
     * @param metrics    EMF MetricsLogger object
     * @param awsRequest DescribeSubscriptionFiltersRequest object (after being translated from a CFN request to service request)
     */
    static void putSubscriptionFilterRequestMetrics(final MetricsLogger metrics, final DescribeSubscriptionFiltersRequest awsRequest) {
        if (Objects.isNull(awsRequest)) return;

        putProperty(metrics, FILTER_NAME_PREFIX, awsRequest.filterNamePrefix());
        putProperty(metrics, LOG_GROUP_NAME, awsRequest.logGroupName());
    }

    /**
     * Helper method to publish properties of a DeleteSubscriptionFilterRequest. Usually, after our handler converts/translates a CFN request to a CWL request
     *
     * @param metrics    EMF MetricsLogger object
     * @param awsRequest DeleteSubscriptionFilterRequest object (after being translated from a CFN request to service request)
     */
    static void putSubscriptionFilterRequestMetrics(final MetricsLogger metrics, final DeleteSubscriptionFilterRequest awsRequest) {
        if (Objects.isNull(awsRequest)) return;

        putProperty(metrics, FILTER_NAME, awsRequest.filterName());
        putProperty(metrics, LOG_GROUP_NAME, awsRequest.logGroupName());
    }
}
