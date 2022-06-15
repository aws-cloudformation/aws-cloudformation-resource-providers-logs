package software.amazon.logs.logstream;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogStreamResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogStreamRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import software.amazon.cloudformation.proxy.delay.Constant;

public class DeleteHandler extends BaseHandlerStd {
    private Logger logger;

//    private static final Constant BACKOFF_DELAY =
//            Constant.of()
//                    .delay(Duration.ofSeconds(10))
//                    .build();

//    private static final Constant BACKOFF_DELAY =
//    Constant.of().delay(Duration.ofSeconds(10)).timeout(Duration.ofMinutes(0)).build();


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

        return proxy.initiate("AWS-Logs-LogStream::Delete", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToDeleteRequest)
                .makeServiceCall((myRequest, myCallbackContext) ->
                {
                    logger.log(String.format("Calling DeleteResource Function with %s", myRequest));
                    return myCallbackContext.injectCredentialsAndInvokeV2(myRequest,myCallbackContext.client()::deleteLogStream);
                })
                .handleError((cbRequest, exception, cbProxyClient, cbModel, cbContext) -> handleError(cbRequest, exception, cbProxyClient, cbModel, cbContext))
                .done(awsResponse -> {
                    logger.log(String.format("% successfully deleted.", ResourceModel.TYPE_NAME));
                    return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .status(OperationStatus.SUCCESS)
                        .build();
                });


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
            final ResourceHandlerRequest<ResourceModel> request) {

        return proxy.initiate("AWS-Logs-LogStream::Delete", proxyClient, model, context)
                .translateToServiceRequest(cbModel -> Translator.translateToDeleteRequest(cbModel))
                .makeServiceCall((cbRequest, cbProxyClient) -> cbProxyClient.injectCredentialsAndInvokeV2(cbRequest, cbProxyClient.client()::createLogStream))
                .handleError((cbRequest, exception, cbProxyClient, cbModel, cbContext) -> handleError(cbRequest, exception, cbProxyClient, cbModel, cbContext))
                .done((cbRequest, cbResponse, cbClient, cbModel, cbContext) -> ProgressEvent.progress(cbModel, cbContext));
    }

//    private DeleteLogStreamResponse deleteResource(final DeleteLogStreamRequest awsRequest, final ProxyClient<CloudWatchLogsClient> proxyClient, final String stackid){
//        DeleteLogStreamResponse deleteLogStreamResponse = null;
//        logger.log("Inside deleteResource Function");
//        try {
//            logger.log("invoke Creds");
//            deleteLogStreamResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteLogStream);
//            logger.log(String.format("Delete response: %s" , deleteLogStreamResponse));
//        } catch (Exception e){
//            logger.log("exception");
////            handleException(e, logger, stackid);
//        }
//        logger.log(String.format("% successfully deleted.", ResourceModel.TYPE_NAME));
//        return deleteLogStreamResponse;
//    }

}

