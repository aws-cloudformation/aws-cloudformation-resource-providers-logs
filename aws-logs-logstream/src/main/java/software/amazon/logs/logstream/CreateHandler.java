package software.amazon.logs.logstream;

import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import com.amazonaws.util.StringUtils;
import com.google.common.annotations.VisibleForTesting;

import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;
import software.amazon.cloudformation.exceptions.*;
import software.amazon.cloudformation.proxy.*;
import software.amazon.cloudformation.resource.IdentifierUtils;

public class CreateHandler extends BaseHandlerStd {
    private Logger logger;

        // * doesResourceWithNameExist
        // * waitForCreate
        // * doPostCreate
        // * pauseWorkflow (10 secs)

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        final String stackId = request.getStackId() == null ? "" : request.getStackId();

        logger.log(String.format("Invoking %s request for model: %s with StackID: %s", "AWS-Logs-LogStream::Create", model, stackId));

        // if log group name is null then return an error message
        if(model == null){
            return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.InvalidRequest, "Model cannot be empty");
        }
        if (StringUtils.isNullOrEmpty(model.getLogGroupName())){
            return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.InvalidRequest, "Log Group Name cannot be empty");
        }
        if (StringUtils.isNullOrEmpty(model.getLogStreamName())) {
            return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.InvalidRequest, "Log Stream Name cannot be empty");
        }

        if (model.getLogStreamName().length() > 512) {
            return ProgressEvent.failed(null, null, HandlerErrorCode.InvalidRequest, "LogStreamName must have length less than or equal to 512");
        } else if(model.getLogStreamName().contains(":") || model.getLogStreamName().contains("*")){
            return ProgressEvent.failed(null, null, HandlerErrorCode.InvalidRequest, "LogStreamName cannot contain the characters ':'(colon) or '*'(asterisk)");
        }

        return ProgressEvent.progress(model, callbackContext)
            .then(progress -> {
                if(progress.getCallbackContext().isItFirstTime()) {
                    progress.getCallbackContext().setItFirstTime(false);
                    return doesResourceExistwithName(proxyClient, model, callbackContext);
                }
                else
                    return progress;
                })
                .then(progress -> createLogStream(proxy, proxyClient, model, callbackContext, request))
                .then(progress -> {
                    if (progress.getCallbackContext().isPropagationDelay()) {
                        logger.log("Propagation delay completed");
                        return ProgressEvent.progress(progress.getResourceModel(), progress.getCallbackContext());
                    }
                    progress.getCallbackContext().setPropagationDelay(true);
                    logger.log("Setting propagation delay");
                   return ProgressEvent.defaultInProgressHandler(progress.getCallbackContext(),
                            EVENTUAL_CONSISTENCY_DELAY_SECONDS, progress.getResourceModel());
                })
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Create Customer Gateway Call Chain. This method creates an Customer Gateway, sets the ID and Value and waits for Stabilization
     * @param proxy
     * @param proxyClient
     * @param model
     * @param context
     * @return ProgressEvent.
     */
    private ProgressEvent<ResourceModel, CallbackContext> createLogStream(final AmazonWebServicesClientProxy proxy,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final ResourceModel model,
            final CallbackContext context,
            final ResourceHandlerRequest<ResourceModel> request) {

        return proxy.initiate("AWS-Logs-LogStream::Create", proxyClient, model, context)
                .translateToServiceRequest(cbModel -> Translator.translateToCreateRequest(cbModel))
                .makeServiceCall((cbRequest, cbProxyClient) -> cbProxyClient.injectCredentialsAndInvokeV2(cbRequest, cbProxyClient.client()::createLogStream))
                .handleError((cbRequest, exception, cbProxyClient, cbModel, cbContext) -> handleError(cbRequest, exception, cbProxyClient, cbModel, cbContext))
                .done((cbRequest, cbResponse, cbClient, cbModel, cbContext) -> ProgressEvent.progress(cbModel, cbContext));
    }
}
