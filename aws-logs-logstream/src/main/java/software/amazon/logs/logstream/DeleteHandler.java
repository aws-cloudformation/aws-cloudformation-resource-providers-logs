package software.amazon.logs.logstream;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogStreamResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogStreamRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.HandlerErrorCode;


import java.time.Duration;

import software.amazon.cloudformation.proxy.delay.Constant;

public class DeleteHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();
        final String stackId = request.getStackId() == null ? "" : request.getStackId();

        logger.log("First log statement");
        logger.log(String.format("Invoking %s request for model: %s with StackID: %s", "AWS-Logs-LogStream::Delete", model, stackId));

        // if log group name is null then return an error message
        if (model == null || StringUtils.isNullOrEmpty(model.getLogGroupName())){
            return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.InvalidRequest, "Log Group Name cannot be empty");
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> deleteLogStream(proxy, proxyClient, model,callbackContext, request, stackId))
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
               .then(progress -> ProgressEvent.<ResourceModel, CallbackContext>builder().status(OperationStatus.SUCCESS).build());
    }

    /**
     * Create Customer Gateway Call Chain. This method creates an Customer Gateway, sets the ID and Value and waits for Stabilization
     * @param proxy
     * @param proxyClient
     * @param model
     * @param context
     * @return ProgressEvent.
     */
    private ProgressEvent<ResourceModel, CallbackContext> deleteLogStream(final AmazonWebServicesClientProxy proxy,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final ResourceModel model,
            final CallbackContext context,
            final ResourceHandlerRequest<ResourceModel> request,
            final String stackId) {

        return proxy.initiate("AWS-Logs-LogStream::Delete", proxyClient, model, context)
                .translateToServiceRequest(cbModel -> Translator.translateToDeleteRequest(cbModel))
                .makeServiceCall((cbRequest, cbProxyClient) -> proxyClient.injectCredentialsAndInvokeV2(cbRequest, cbProxyClient.client()::deleteLogStream))
                .handleError((cbRequest, exception, cbProxyClient, cbModel, cbContext) -> handleError(cbRequest, exception, cbProxyClient, cbModel, cbContext))
                .done((cbRequest, cbResponse, cbClient, cbModel, cbContext) -> ProgressEvent.progress(cbModel, cbContext));
    }

}

