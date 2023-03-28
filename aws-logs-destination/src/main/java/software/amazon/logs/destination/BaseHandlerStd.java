package software.amazon.logs.destination;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationPolicyResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.LimitExceededException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.CallChain;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.NoSuchElementException;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        return handleRequest(
                proxy,
                request,
                callbackContext != null ? callbackContext : new CallbackContext(),
                proxy.newProxy(ClientBuilder::getClient),
                logger
        );
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger);

    protected CallChain.Completed<DescribeDestinationsRequest, DescribeDestinationsResponse, CloudWatchLogsClient, ResourceModel, CallbackContext> preCreateCheck(
            final AmazonWebServicesClientProxy proxy, final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient, final ResourceModel model) {
        return proxy.initiate("AWS-Logs-Destination::Create::PreExistenceCheck", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((awsRequest, sdkProxyClient) -> sdkProxyClient.injectCredentialsAndInvokeV2(awsRequest,
                        sdkProxyClient.client()::describeDestinations))
                .handleError((request, exception, client, model1, context1) -> {
                    ProgressEvent<ResourceModel, CallbackContext> progress;
                    if (exception instanceof InvalidParameterException) {
                        progress = ProgressEvent.failed(model, callbackContext, HandlerErrorCode.InvalidRequest,
                                exception.getMessage());
                    } else if (exception instanceof ServiceUnavailableException) {
                        progress = ProgressEvent.failed(model, callbackContext, HandlerErrorCode.ServiceInternalError,
                                exception.getMessage());
                    } else if (exception instanceof ResourceNotFoundException) {
                        progress = ProgressEvent.progress(model, callbackContext);
                    } else if (exception instanceof CloudWatchLogsException) {
                        progress =
                                ProgressEvent.failed(model, callbackContext, HandlerErrorCode.GeneralServiceException,
                                        exception.getMessage());
                    } else {
                        throw exception;
                    }
                    return progress;
                });
    }

    protected boolean destinationNameExists(final DescribeDestinationsResponse response, ResourceModel model) {
        if (response == null || response.destinations() == null) {
            return false;
        }
        if (!response.hasDestinations()) {
            return false;
        }
        if (response.destinations().isEmpty()) {
            return false;
        }
        return model.getDestinationName().equals(response.destinations().get(0).destinationName());
    }

    protected ProgressEvent<ResourceModel, CallbackContext> putDestination(final AmazonWebServicesClientProxy proxy,
                                                                           final CallbackContext callbackContext,
                                                                           final ProxyClient<CloudWatchLogsClient> proxyClient,
                                                                           final ResourceModel model,
                                                                           String stackId,
                                                                           final String callGraph,
                                                                           final Logger logger) {
        return proxy.initiate(callGraph, proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToPutDestinationRequest)
                .makeServiceCall((destinationRequest, client) -> client
                        .injectCredentialsAndInvokeV2(destinationRequest,
                                client.client()::putDestination))
                .handleError((res, e, proxyClient1, model1, context) -> {
                    // Check if multiple concurrent requests to update the same resource are in conflict
                    if (e instanceof OperationAbortedException) {
                        return ProgressEvent.defaultInProgressHandler(context, 5, model);
                    }

                    final HandlerErrorCode errorCode = getExceptionDetails(e, logger, stackId);
                    return ProgressEvent.defaultFailureHandler(e, errorCode);
                })
                .progress();
    }

    protected ProgressEvent<ResourceModel, CallbackContext> putDestinationPolicy(
            final AmazonWebServicesClientProxy proxy, final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient, final ResourceModel model, final String callGraph,
            final Logger logger, Action handlerAction) {
        return proxy.initiate(callGraph, proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToPutDestinationPolicyRequest)
                .makeServiceCall((awsRequest, sdkProxyClient) -> {
                    PutDestinationPolicyResponse response = null;
                    try {
                        response = sdkProxyClient.injectCredentialsAndInvokeV2(awsRequest,
                                sdkProxyClient.client()::putDestinationPolicy);
                        logger.log(String.format(
                                "Destination policy successfully updated for the resource with name %s has been " +
                                        "successfully %s", model.getDestinationName(), handlerAction.name()));
                    } catch (CloudWatchLogsException e) {
                        logger.log(String.format(
                                "Exception while invoking the putDestinationPolicy API for the destination ID %s. %s ",
                                model.getDestinationName(), e));
                        Translator.translateException(e);
                    }
                    return response;
                })
                .progress();
    }

    protected HandlerErrorCode getExceptionDetails(final Exception e, final Logger logger, final String stackId) {
        HandlerErrorCode errorCode = HandlerErrorCode.GeneralServiceException;
        if (e instanceof InvalidParameterException) {
            errorCode = HandlerErrorCode.InvalidRequest;
        } else if (e instanceof LimitExceededException) {
            errorCode = HandlerErrorCode.ServiceLimitExceeded;
        } else if (e instanceof ServiceUnavailableException) {
            errorCode = HandlerErrorCode.InternalFailure;
        } else if (e instanceof NoSuchElementException) {
            errorCode = HandlerErrorCode.NotFound;
        }

        logExceptionDetails(e, logger, stackId);
        return errorCode;
    }

    /**
     * Log the details of the exception thrown
     *
     * @param e       - the exception
     * @param logger  - a reference to the logger
     * @param stackId - the id of the stack where the exception was thrown
     */
    protected void logExceptionDetails(Exception e, Logger logger, final String stackId) {
        logger.log(String.format("Stack with ID: %s got exception: %s Message: %s Cause: %s", stackId,
                e.toString(), e.getMessage(), e.getCause()));
    }
}
