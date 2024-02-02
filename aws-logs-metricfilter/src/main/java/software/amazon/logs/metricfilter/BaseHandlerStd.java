package software.amazon.logs.metricfilter;

import static software.amazon.awssdk.core.internal.retry.SdkDefaultRetrySetting.RETRYABLE_STATUS_CODES;
import static software.amazon.logs.common.MetricsConstants.CFN;
import static software.amazon.logs.common.MetricsConstants.SERVICE;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Locale;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.RetryableException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.delay.Exponential;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;
import software.amazon.logs.common.MetricsHelper;
import software.amazon.logs.common.MetricsProvider;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

    // Uluru level exponential back off strategy. Starting with min delay of 5s, until 30s is reached.
    Exponential getBackOffStrategy() {
        return Exponential.of().minDelay(Duration.ofSeconds(5)).timeout(Duration.ofSeconds(30)).build();
    }

    protected static final int CALLBACK_DELAY_SECONDS = 1;

    ProgressEvent<ResourceModel, CallbackContext> handleError(Exception exception, ResourceModel resourceModel, CallbackContext context) {
        if (exception.getMessage().toLowerCase(Locale.ROOT).contains("rate exceeded")) {
            throw RetryableException.builder().cause(exception).build();
        }
        if (exception instanceof AwsServiceException && RETRYABLE_STATUS_CODES.contains(((AwsServiceException) exception).statusCode())) {
            throw RetryableException.builder().cause(exception).build();
        }
        return ProgressEvent.failed(resourceModel, context, HandlerErrorCode.GeneralServiceException, exception.getMessage());
    }

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger
    ) {
        final long startTime = System.currentTimeMillis();
        MetricsLogger metricsLogger = MetricsProvider.getMetrics();
        MetricsHelper.putCFNRequestProperties(metricsLogger, request);

        ProgressEvent<ResourceModel, CallbackContext> result;
        try {
            result =
                    handleRequest(
                            proxy,
                            request,
                            callbackContext != null ? callbackContext : new CallbackContext(),
                            proxy.newProxy(ClientBuilder::getClient),
                            logger,
                            MetricsProvider.getMetrics()
                    );
            MetricsHelper.putCFNProperties(metricsLogger, result);
        } finally {
            MetricsHelper.putTime(metricsLogger, startTime, System.currentTimeMillis());
            MetricsHelper.flush(metricsLogger);
        }

        return result;
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger,
            final MetricsLogger metrics
    );

    /**
     * Checks if the Metric Filter exists.
     *
     * @param proxyClient The proxy client used to execute API calls.
     * @param model       The resource model representing the metric filter.
     * @param handlerAction The action being performed by the handler.
     * @param logger      The logger.
     * @param metrics     The metrics logger.
     * @return True if the resource exists, false otherwise.
     * @throws BaseHandlerException If an exception occurs while checking if the resource exists.
     */
    protected boolean exists(
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final ResourceModel model,
            final Action handlerAction,
            final Logger logger,
            final MetricsLogger metrics
    ) {
        final DescribeMetricFiltersRequest translateToReadRequest = Translator.translateToReadRequest(model);
        final DescribeMetricFiltersResponse response;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(translateToReadRequest, proxyClient.client()::describeMetricFilters);
            if (response == null || !response.hasMetricFilters() || response.metricFilters().isEmpty()) {
                return false;
            }

            return model.getFilterName().equals(response.metricFilters().get(0).filterName());
        } catch (final ResourceNotFoundException ignored) {
            return false;
        } catch (final CloudWatchLogsException serviceException) {
            BaseHandlerException handlerException = Translator.translateException(serviceException);

            String serviceExceptionStackTrace = exceptionStackTrace(serviceException);
            logger.log(
                    String.format(
                            "[%s][EXCEPTION] Encountered exception while checking if metric filter %s exists: %s: %s. %s",
                            handlerAction,
                            model.getFilterName(),
                            serviceException.getClass().getSimpleName(),
                            serviceException.getMessage(),
                            serviceExceptionStackTrace
                    )
            );

            MetricsHelper.putExceptionProperty(metrics, serviceException, SERVICE);
            MetricsHelper.putExceptionProperty(metrics, handlerException, CFN);

            throw handlerException;
        }
    }

    /**
     * Returns the stack trace of the given exception as a string.
     *
     * @param t The exception.
     * @return The stack trace of the given exception as a string.
     */
    public static String exceptionStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
            return sw.toString();
        }
    }
}
