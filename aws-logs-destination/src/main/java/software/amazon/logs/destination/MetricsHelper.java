package software.amazon.logs.destination;

import static software.amazon.logs.common.MetricsHelper.putProperty;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteDestinationRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;

/**
 * Helper class specific to Destination resource to be used with an EMF MetricsLogger object to publish metrics and properties
 */
@Slf4j
final class MetricsHelper {

    public static final String DESTINATION_NAME = "DestinationName";
    private static final String DESTINATION_NAME_PREFIX = "DestinationNamePrefix";

    private MetricsHelper() {}

    /**
     * Helper method to publish properties of a PutDestinationRequest. Usually, after our handler converts/translates a CFN request to a CWL request
     *
     * @param metrics    EMF MetricsLogger object
     * @param awsRequest PutDestinationRequest object (after being translated from a CFN request to service request)
     */
    static void putDestinationRequestMetrics(final MetricsLogger metrics, final PutDestinationRequest awsRequest) {
        if (Objects.isNull(awsRequest)) return;

        putProperty(metrics, DESTINATION_NAME, awsRequest.destinationName());
    }

    /**
     * Helper method to publish properties of a DescribeDestinationRequest. Usually, after our handler converts/translates a CFN request to a CWL request
     *
     * @param metrics    EMF MetricsLogger object
     * @param awsRequest DescribeDestinationRequest object (after being translated from a CFN request to service request)
     */
    static void putDestinationRequestMetrics(final MetricsLogger metrics, final DescribeDestinationsRequest awsRequest) {
        if (Objects.isNull(awsRequest)) return;

        putProperty(metrics, DESTINATION_NAME_PREFIX, awsRequest.destinationNamePrefix());
    }

    /**
     * Helper method to publish properties of a DeleteDestinationRequest. Usually, after our handler converts/translates a CFN request to a CWL request
     *
     * @param metrics    EMF MetricsLogger object
     * @param awsRequest DeleteDestinationRequest object (after being translated from a CFN request to service request)
     */
    static void putDestinationRequestMetrics(final MetricsLogger metrics, final DeleteDestinationRequest awsRequest) {
        if (Objects.isNull(awsRequest)) return;

        putProperty(metrics, DESTINATION_NAME, awsRequest.destinationName());
    }
}
