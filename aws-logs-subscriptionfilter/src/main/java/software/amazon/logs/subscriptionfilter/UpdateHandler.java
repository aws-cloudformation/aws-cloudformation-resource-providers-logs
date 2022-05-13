package software.amazon.logs.subscriptionfilter;

import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;
import software.amazon.cloudformation.exceptions.*;
import software.amazon.cloudformation.proxy.*;

public class UpdateHandler extends BaseHandlerStd {
    private static final String callGraphString = "AWS-AutoScaling-LifecycleHook::Update";
    private Logger logger;
    private final ReadHandler readHandler;

    public UpdateHandler() {
        super();
        readHandler = new ReadHandler();
    }

    @VisibleForTesting
    protected UpdateHandler(CloudWatchLogsClient cloudWatchLogsClient, ReadHandler readHandler) {
        super(cloudWatchLogsClient);
        this.readHandler = readHandler;
    }

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> {
                    try {
                        ProgressEvent<ResourceModel, CallbackContext> readProgressEvent =
                                readHandler.handleRequest(proxy, request, callbackContext, proxyClient, logger);
                        return readProgressEvent;
                    } catch (CfnNotFoundException e) {
                        return ProgressEvent.defaultFailureHandler(e, HandlerErrorCode.NotFound);
                    }
                })
                .onSuccess(progress ->
                        proxy.initiate(callGraphString, proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToUpdateRequest)
                                .makeServiceCall((putLifecycleHookRequest, client) -> client
                                        .injectCredentialsAndInvokeV2(putLifecycleHookRequest,
                                                client.client()::putSubscriptionFilter))
                                .handleError((autoScalingRequest, e, proxyClient1, model1, context) -> {
                                    HandlerErrorCode errorCode = HandlerErrorCode.GeneralServiceException;
                                    if (e instanceof InvalidParameterException) {
                                        errorCode = HandlerErrorCode.InvalidRequest;
                                    } else if (e instanceof LimitExceededException) {
                                        errorCode = HandlerErrorCode.ServiceLimitExceeded;
                                    } else if (e instanceof ServiceUnavailableException) {
                                        errorCode = HandlerErrorCode.InternalFailure;
                                    }

                                    return ProgressEvent.defaultFailureHandler(e, errorCode);
                                })
                                .progress()
                )
                .then(progress -> readHandler
                        .handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private PutSubscriptionFilterResponse updateResource(
            final ResourceModel model,
            final PutSubscriptionFilterRequest awsRequest,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final String stackId) {
        PutSubscriptionFilterResponse awsResponse;
        try {
            boolean exists = doesResourceExist(proxyClient, model);
            if (!exists) {
                logger.log(String.format("Resource does not exist for request: %s in stack %s", awsRequest.toString(), stackId));
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
            }

            logger.log(String.format("Resource exists; attempting updating for request: %s in stack %s", awsRequest.toString(), stackId));
            return proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::putSubscriptionFilter);
        } catch (final ResourceNotFoundException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnNotFoundException(e);
        } catch (final InvalidParameterException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
        } catch (final LimitExceededException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnServiceLimitExceededException(e);
        } catch (final ServiceUnavailableException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnServiceInternalErrorException(e);
        } catch (final OperationAbortedException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnResourceConflictException(e);
        }
    }
}
