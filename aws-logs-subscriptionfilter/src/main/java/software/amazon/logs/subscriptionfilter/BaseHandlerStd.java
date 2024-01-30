package software.amazon.logs.subscriptionfilter;

import static software.amazon.awssdk.core.internal.retry.SdkDefaultRetrySetting.RETRYABLE_STATUS_CODES;
import static software.amazon.logs.common.MetricsConstants.CFN;
import static software.amazon.logs.common.MetricsConstants.SERVICE;
import static software.amazon.logs.subscriptionfilter.MetricsHelper.putSubscriptionFilterRequestMetrics;

import java.time.Duration;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutSubscriptionFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutSubscriptionFilterResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
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

    private static final String ERROR_CODE_INVALID_PARAMETER_EXCEPTION = "InvalidParameterException";

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
     * Checks if the given exception is a retryable exception.
     *
     * @param exception The exception to be checked.
     * @return true if the exception is retryable, false otherwise.
     */
    boolean isRetryableException(final Exception exception) {
        boolean retryableException =
            exception instanceof AwsServiceException && RETRYABLE_STATUS_CODES.contains(((AwsServiceException) exception).statusCode());
        boolean isInvalidParameterException =
            exception instanceof AwsServiceException &&
            ((AwsServiceException) exception).awsErrorDetails() != null &&
            ((AwsServiceException) exception).awsErrorDetails().errorCode().equals(ERROR_CODE_INVALID_PARAMETER_EXCEPTION);

        return retryableException || isInvalidParameterException;
    }

    /**
     * Creates a Subscription Filter
     *
     * @param model         The resource model representing the subscription filter.
     * @param awsRequest    The request object used to create the subscription filter.
     * @param proxyClient   The proxy client used to execute API calls.
     * @param handlerAction The action being performed by the handler.
     * @param logger        The logger.
     * @param metrics       The metrics logger.
     * @return The response object containing the created subscription filter.
     * @throws BaseHandlerException If an exception occurs while creating the resource.
     */
    protected PutSubscriptionFilterResponse putResource(
        final ResourceModel model,
        final PutSubscriptionFilterRequest awsRequest,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Action handlerAction,
        final Logger logger,
        final MetricsLogger metrics
    ) throws BaseHandlerException {
        PutSubscriptionFilterResponse awsResponse;
        putSubscriptionFilterRequestMetrics(metrics, awsRequest);

        final String filterName = awsRequest.filterName();
        final String logGroupName = awsRequest.logGroupName();

        try {
            boolean exists = exists(proxyClient, model, handlerAction, logger, metrics);

            if (exists && handlerAction.equals(Action.CREATE)) {
                logger.log(
                    String.format("[CREATE][FAILED] SubscriptionFilter %s already exists in log group %s", filterName, logGroupName)
                );
                throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
            }

            if (!exists && handlerAction.equals(Action.UPDATE)) {
                logger.log(
                    String.format("[UPDATE][FAILED] SubscriptionFilter %s does not exist in log group %s", filterName, logGroupName)
                );
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
            }

            // Create/Update Subscription Filter
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::putSubscriptionFilter);
        } catch (final CloudWatchLogsException serviceException) {
            final BaseHandlerException handlerException = Translator.translateException(serviceException);

            logger.log(
                String.format(
                    "[%s][EXCEPTION] Encountered exception with SubscriptionFilter %s in log group %s: %s: %s",
                    handlerAction.name(),
                    filterName,
                    logGroupName,
                    serviceException.getClass().getSimpleName(),
                    serviceException.getMessage()
                )
            );

            MetricsHelper.putExceptionProperty(metrics, serviceException, SERVICE);
            MetricsHelper.putExceptionProperty(metrics, handlerException, CFN);

            throw handlerException;
        }

        MetricsHelper.putServiceMetrics(metrics, awsResponse);
        logger.log(String.format("[CREATE][SUCCESS] Created new subscription filter %s in log group %s", filterName, logGroupName));

        return awsResponse;
    }

    /**
     * Checks if the Subscription Filter exists.
     *
     * @param proxyClient The proxy client used to execute API calls.
     * @param model       The resource model representing the subscription filter.
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
        final DescribeSubscriptionFiltersRequest translateToReadRequest = Translator.translateToReadRequest(model);
        final DescribeSubscriptionFiltersResponse response;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(translateToReadRequest, proxyClient.client()::describeSubscriptionFilters);
            if (response == null || !response.hasSubscriptionFilters() || response.subscriptionFilters().isEmpty()) {
                return false;
            }

            return model.getFilterName().equals(response.subscriptionFilters().get(0).filterName());
        } catch (final ResourceNotFoundException ignored) {
            return false;
        } catch (final CloudWatchLogsException serviceException) {
            BaseHandlerException handlerException = Translator.translateException(serviceException);

            logger.log(
                String.format(
                    "[%s][EXCEPTION] Encountered exception while checking if subscription filter %s exists: %s: %s",
                    handlerAction,
                    model.getFilterName(),
                    serviceException.getClass().getSimpleName(),
                    serviceException.getMessage()
                )
            );

            MetricsHelper.putExceptionProperty(metrics, serviceException, SERVICE);
            MetricsHelper.putExceptionProperty(metrics, handlerException, CFN);

            throw handlerException;
        }
    }
}
