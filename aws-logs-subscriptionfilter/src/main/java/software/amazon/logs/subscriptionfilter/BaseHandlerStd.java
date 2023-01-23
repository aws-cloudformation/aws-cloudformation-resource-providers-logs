package software.amazon.logs.subscriptionfilter;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.AbortedException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.LimitExceededException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceAlreadyExistsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.CallChain;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNull;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

    private final CloudWatchLogsClient cloudWatchLogsClient;

    protected BaseHandlerStd() {
        this(ClientBuilder.getClient());
    }

    protected BaseHandlerStd(CloudWatchLogsClient cloudWatchLogsClient) {
        this.cloudWatchLogsClient = requireNonNull(cloudWatchLogsClient);
    }

    private CloudWatchLogsClient getCloudWatchLogsClient() {
        return cloudWatchLogsClient;
    }

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

    /**
     * Log the details of the exception thrown
     *
     * @param e - the exception
     * @param logger - a reference to the logger
     * @param stackId - the id of the stack where the exception was thrown
     */
    protected void logExceptionDetails(Exception e, Logger logger, final String stackId) {
        logger.log(String.format("Stack with ID: %s got exception: %s Message: %s Cause: %s", stackId,
                e.toString(), e.getMessage(), e.getCause()));
    }

    protected boolean isAccessDeniedError(Exception e, final Logger logger) {
        logger.log(String.format("Got exception in AccessDenied Check: Exception: %s Message: %s, Cause: %s", e,
                e.getMessage(), e.getCause()));

        if (e instanceof CloudWatchLogsException) {
            if  (e.getMessage() != null && e.getMessage().contains("is not authorized to perform: logs:")) {
                logger.log("AccessDenied exception in AccessDeniedCheck, passing");
                return true;
            }
        }

        final String ACCESS_DENIED_ERROR = "AccessDenied";
        if (e instanceof AwsServiceException && ((AwsServiceException)e).awsErrorDetails() != null ) {
            final AwsServiceException awsServiceException = (AwsServiceException) e;
            return awsServiceException.awsErrorDetails().errorCode().equals(ACCESS_DENIED_ERROR);
        } else {
            if (e.getMessage() != null) {
                return e.getMessage().equals(ACCESS_DENIED_ERROR);
            } else {
                return false;
            }
        }
    }

    protected void handleException(Exception e, Logger logger, final String stackId) {
        logExceptionDetails(e, logger, stackId);

        if (e instanceof InvalidParameterException) {
            throw new CfnInvalidRequestException(String.format("%s. %s", ResourceModel.TYPE_NAME, e.getMessage()), e);
        } else if (e instanceof ResourceAlreadyExistsException) {
            throw new CfnAlreadyExistsException(e);
        } else if (e instanceof ResourceNotFoundException) {
            throw new CfnNotFoundException(e);
        } else if (e instanceof ServiceUnavailableException) {
            throw new CfnServiceInternalErrorException(e);
        } else if (e instanceof LimitExceededException) {
            throw new CfnServiceLimitExceededException(e);
        } else if (isAccessDeniedError(e, logger)) {
            throw new CfnAccessDeniedException(e);
        }
        throw new CfnGeneralServiceException(e);
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
     * Check if a RetryException should be thrown; returns true if InvalidParameterException, AbortedException,
     * or "OperationAbortedException"
     *
     * @param e - the exception
     * @return - true if should be retried (exception thrown), false otherwise
     */
    protected boolean shouldThrowRetryException(final Exception e) {
        final String ERROR_CODE_OPERATION_ABORTED_EXCEPTION = "OperationAbortedException";
        if (e instanceof AwsServiceException && ((AwsServiceException)e).awsErrorDetails() != null ) {
            final AwsServiceException awsServiceException = (AwsServiceException) e;
            return awsServiceException.awsErrorDetails().errorCode().equals(ERROR_CODE_OPERATION_ABORTED_EXCEPTION);
        }

        return e instanceof InvalidParameterException || e instanceof AbortedException
                || e.getMessage().equals(ERROR_CODE_OPERATION_ABORTED_EXCEPTION);
    }

    protected CallChain.Completed<DescribeSubscriptionFiltersRequest, DescribeSubscriptionFiltersResponse, CloudWatchLogsClient, ResourceModel, CallbackContext> preCreateCheck(
            final AmazonWebServicesClientProxy proxy, final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient, final ResourceModel model) {
        return proxy.initiate("AWS-Logs-SubscriptionFilter::Create::PreExistenceCheck", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((awsRequest, sdkProxyClient) -> sdkProxyClient.injectCredentialsAndInvokeV2(awsRequest,
                        sdkProxyClient.client()::describeSubscriptionFilters))
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

    protected boolean filterNameExists(final DescribeSubscriptionFiltersResponse response, ResourceModel model) {
        if (response == null || response.subscriptionFilters() == null) {
            return false;
        }
        if (!response.hasSubscriptionFilters()) {
            return false;
        }
        if (response.subscriptionFilters().isEmpty()) {
            return false;
        }
        return model.getFilterName().equals(response.subscriptionFilters().get(0).filterName());
    }
}
