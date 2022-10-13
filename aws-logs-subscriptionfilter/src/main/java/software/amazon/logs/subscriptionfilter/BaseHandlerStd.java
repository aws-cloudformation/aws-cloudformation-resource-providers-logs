package software.amazon.logs.subscriptionfilter;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.AbortedException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.*;
import software.amazon.cloudformation.proxy.*;

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
            if  (e.getMessage() != null && e.getMessage().contains("is not authorized to perform: logs:DescribeSubscriptionFilters")) {
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
            throw new CfnInvalidRequestException(e);
        } else if (e instanceof ResourceNotFoundException) {
            throw new CfnNotFoundException(e);
        } else if (e instanceof ServiceUnavailableException) {
            throw new CfnServiceInternalErrorException(e);
        }
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
}
