package software.amazon.logs.common;

import static software.amazon.logs.common.MetricsConstants.CFN;
import static software.amazon.logs.common.MetricsConstants.SERVICE;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsResponseMetadata;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;
import software.amazon.cloudwatchlogs.emf.model.StorageResolution;
import software.amazon.cloudwatchlogs.emf.model.Unit;

/**
 * Helper class to be used with an EMF MetricsLogger object to publish metrics and properties
 */
@Slf4j
public final class MetricsHelper {

    private MetricsHelper() {}

    /**
     * @param metrics EMF MetricsLogger object
     * @param name    of property to publish
     * @param value   of property
     */
    public static void putProperty(final MetricsLogger metrics, final String name, final Object value) {
        if (Objects.isNull(value)) return;
        if (value instanceof String && StringUtils.isBlank(value.toString())) return;

        metrics.putProperty(name, value);
    }

    /**
     * @param metrics    EMF MetricsLogger object
     * @param name       of the metric to publish
     * @param value      of the metric
     * @param unit       of the metric
     * @param resolution of the metric
     */
    public static void putMetric(
        final MetricsLogger metrics,
        final String name,
        final double value,
        final Unit unit,
        final StorageResolution resolution
    ) {
        try {
            metrics.putMetric(name, value, unit, resolution);
        } catch (final Exception ex) {
            log.error("Failed to emit metric [{}] with value [{}]", name, value, ex);
        }
    }

    /**
     * @param metrics EMF MetricsLogger object
     * @param name    of the metric to publish
     * @param value   of the metric
     * @param unit    of the metric
     */
    public static void putMetric(final MetricsLogger metrics, final String name, final double value, final Unit unit) {
        putMetric(metrics, name, value, unit, StorageResolution.STANDARD);
    }

    /**
     * @param metrics EMF MetricsLogger object
     * @param name    of the metric to publish
     * @param value   of the metric
     */
    public static void putMetric(final MetricsLogger metrics, final String name, final double value) {
        putMetric(metrics, name, value, Unit.NONE);
    }

    /**
     * Helper method to quickly publish properties of an exception, such as the exception class, exception message, and exception stack trace
     *
     * @param metrics   EMF MetricsLogger object
     * @param exception object
     * @param prefix    of the metric name
     */
    public static void putExceptionProperty(final MetricsLogger metrics, final Exception exception, final String prefix) {
        final String stackTrace = ExceptionUtils.getStackTrace(exception);
        putProperty(metrics, String.format("%s.Exception", prefix), exception.getClass().getSimpleName());
        putProperty(metrics, String.format("%s.ExceptionMessage", prefix), exception.getMessage());
        putProperty(metrics, String.format("%s.StackTrace", prefix), stackTrace);
    }

    /**
     * Helper method to quickly publish a metric between two timestamps in milliseconds
     *
     * @param metrics   EMF MetricsLogger object
     * @param startTime start timestamp in milliseconds
     * @param endTime   end timestamp in milliseconds
     */
    public static void putTime(final MetricsLogger metrics, final long startTime, final long endTime) {
        putMetric(metrics, "Time", endTime - startTime, Unit.MILLISECONDS);
    }

    /**
     * Flush the MetricsLogger object to STDOUT
     *
     * @param metrics EMF MetricsLogger object
     */
    public static void flush(final MetricsLogger metrics) {
        metrics.flush();
    }

    /**
     * A helper method to quickly publish various properties from an SDK client response
     *
     * @param metrics  EMF MetricsLogger object
     * @param response a CWL response object
     */
    public static void putServiceMetrics(final MetricsLogger metrics, final CloudWatchLogsResponse response) {
        if (Objects.isNull(response)) return;

        final CloudWatchLogsResponseMetadata responseMetadata = response.responseMetadata();
        final SdkHttpResponse sdkHttpResponse = response.sdkHttpResponse();

        if (Objects.nonNull(responseMetadata)) {
            putProperty(metrics, String.format("%s.RequestId", SERVICE), responseMetadata.requestId());
        }
        if (Objects.nonNull(sdkHttpResponse)) {
            putMetric(metrics, String.format("%s.Success", SERVICE), sdkHttpResponse.isSuccessful() ? 1 : 0);
            putProperty(metrics, String.format("%s.StatusCode", SERVICE), sdkHttpResponse.statusCode());
            putProperty(metrics, String.format("%s.StatusText", SERVICE), sdkHttpResponse.statusText());
        }
    }

    /**
     * Helper method to quickly publish properties from the CFN handler side. These are part of the response returned to the customer.
     *
     * @param metrics EMF MetricsLogger object
     * @param result  from the handler call
     */
    public static void putCFNProperties(final MetricsLogger metrics, final ProgressEvent<?, ?> result) {
        if (Objects.isNull(result)) return;

        final HandlerErrorCode errorCode = result.getErrorCode();
        final OperationStatus status = result.getStatus();

        putMetric(metrics, String.format("%s.Success", CFN), result.isSuccess() ? 1 : 0);
        putProperty(metrics, String.format("%s.Message", CFN), result.getMessage());
        putProperty(metrics, String.format("%s.Result", CFN), result.getResult());
        if (Objects.nonNull(errorCode)) {
            putProperty(metrics, String.format("%s.ErrorCode", CFN), errorCode.name());
        }
        if (Objects.nonNull(status)) {
            putProperty(metrics, String.format("%s.Status", CFN), status.name());
        }
    }

    /**
     * Helper method to publish properties from the CFN request made by the customer
     *
     * @param metrics EMF MetricsLogger object
     * @param request ResourceHandlerRequest object
     */
    public static void putCFNRequestProperties(final MetricsLogger metrics, final ResourceHandlerRequest<?> request) {
        putProperty(metrics, "AwsAccountId", request.getAwsAccountId());
        putProperty(metrics, String.format("%s.RequestToken", CFN), request.getClientRequestToken());
        putProperty(metrics, String.format("%s.StackId", CFN), request.getStackId());
    }
}
