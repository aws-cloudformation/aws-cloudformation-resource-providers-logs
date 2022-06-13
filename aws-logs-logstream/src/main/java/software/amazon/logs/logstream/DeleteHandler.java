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
//                .backoffDelay(BACKOFF_DELAY)
                .makeServiceCall((myRequest, myCallbackContext) ->
                {
                    logger.log("MakeService Call");
                    return deleteResource(myRequest, proxyClient, stackId);
                })
                .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .status(OperationStatus.SUCCESS)
                        .build());

    }

    private DeleteLogStreamResponse deleteResource(final DeleteLogStreamRequest awsRequest, final ProxyClient<CloudWatchLogsClient> proxyClient, final String stackid){
        DeleteLogStreamResponse deleteLogStreamResponse = null;
        logger.log("Inside deleteResource Function");
        try {
            logger.log("invoke Creds");
            deleteLogStreamResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteLogStream);
        } catch (Exception e){
            logger.log("exception");
            handleException(e, logger, stackid);
        }
        logger.log(String.format("% successfully deleted.", ResourceModel.TYPE_NAME));
        return deleteLogStreamResponse;
    }

}

