package software.amazon.logs.destination;

import static software.amazon.awssdk.core.internal.retry.SdkDefaultRetrySetting.RETRYABLE_STATUS_CODES;
import static software.amazon.logs.destination.MetricsHelper.putDestinationRequestMetrics;

import java.time.Duration;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationResponse;
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
import software.amazon.logs.common.MetricsConstants;
import software.amazon.logs.common.MetricsHelper;
import software.amazon.logs.common.MetricsProvider;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

    // Uluru level exponential back off strategy. Starting with a min delay of 5s, until 30s is reached.
    Exponential getBackOffStrategy() {
        return Exponential.of().minDelay(Duration.ofSeconds(5)).timeout(Duration.ofSeconds(30)).build();
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
                    metricsLogger
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
     * Checks if a Destination exists.
     *
     * @param proxyClient The proxy client used to execute API calls.
     * @param model       The resource model representing the destination.
     * @return True if the destination exists, false otherwise.
     */
    protected boolean exists(final ProxyClient<CloudWatchLogsClient> proxyClient, final ResourceModel model) {
        final DescribeDestinationsRequest translateToReadRequest = Translator.translateToReadRequest(model);
        final DescribeDestinationsResponse response;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(translateToReadRequest, proxyClient.client()::describeDestinations);
            if (response == null || !response.hasDestinations() || response.destinations().isEmpty()) {
                return false;
            }

            return model.getDestinationName().equals(response.destinations().get(0).destinationName());
        } catch (final ResourceNotFoundException e) {
            return false;
        }
    }

    /**
     * Creates a Destination
     *
     * @param model         The resource model representing the destination.
     * @param awsRequest    The request object used to create the destination.
     * @param proxyClient   The proxy client used to execute API calls.
     * @param logger        The logger.
     * @param metrics       The metrics logger.
     * @param handlerAction The action being performed by the handler.
     * @return The response object containing the created destination.
     * @throws BaseHandlerException If an exception occurs while creating the resource.
     */
    protected PutDestinationResponse putDestination(
        final ResourceModel model,
        final PutDestinationRequest awsRequest,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger,
        final Action handlerAction,
        final MetricsLogger metrics
    ) throws BaseHandlerException {
        PutDestinationResponse awsResponse;
        putDestinationRequestMetrics(metrics, awsRequest);

        try {
            boolean exists = exists(proxyClient, model);

            if (exists && handlerAction.equals(Action.CREATE)) {
                logger.log(String.format("[CREATE][FAILED] Destination %s already exists", model.getDestinationName()));
                throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
            }

            if (!exists && handlerAction.equals(Action.UPDATE)) {
                logger.log(String.format("[UPDATE][FAILED] Destination %s does not exist", model.getDestinationName()));
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
            }

            // Create/update destination
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::putDestination);

            // Create/update destination policy
            if (model.getDestinationPolicy() != null) {
                PutDestinationPolicyRequest policyRequest = Translator.translateToPutDestinationPolicyRequest(model);
                proxyClient.injectCredentialsAndInvokeV2(policyRequest, proxyClient.client()::putDestinationPolicy);

                logger.log(
                    String.format(
                        "[%s][SUCCESS] Destination policy successfully updated for the destination with name %s",
                        handlerAction.name(),
                        model.getDestinationName()
                    )
                );
            }
        } catch (final CloudWatchLogsException serviceException) {
            final BaseHandlerException handlerException = Translator.translateException(serviceException);

            logger.log(
                String.format(
                    "[%s][EXCEPTION] Encountered exception with destination %s: %s: %s",
                    handlerAction.name(),
                    model.getDestinationName(),
                    serviceException.getClass().getSimpleName(),
                    serviceException.getMessage()
                )
            );

            MetricsHelper.putExceptionProperty(metrics, serviceException, MetricsConstants.SERVICE);
            MetricsHelper.putExceptionProperty(metrics, handlerException, MetricsConstants.CFN);

            throw handlerException;
        }

        logger.log(String.format("[%s][SUCCESS] Created new destination %s", handlerAction.name(), model.getDestinationName()));
        MetricsHelper.putServiceMetrics(metrics, awsResponse);

        return awsResponse;
    }

    /**
     * Checks if the given exception is a retryable exception.
     *
     * @param exception The exception to be checked.
     * @return true if the exception is retryable, false otherwise.
     */
    boolean isRetryableException(final Exception exception) {
        return (
            exception instanceof AwsServiceException && RETRYABLE_STATUS_CODES.contains(((AwsServiceException) exception).statusCode())
        );
    }
}
