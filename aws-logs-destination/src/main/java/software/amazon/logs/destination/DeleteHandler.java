package software.amazon.logs.destination;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteDestinationRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteDestinationResponse;
import software.amazon.cloudformation.proxy.*;

public class DeleteHandler extends BaseHandlerStd {

    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {
        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        return proxy.initiate("AWS-Logs-Destination::Delete", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToDeleteRequest)
                .makeServiceCall(this::deleteResource)
                .done((x)-> ProgressEvent.<ResourceModel, CallbackContext>builder().status(OperationStatus.SUCCESS).build());
    }

    private DeleteDestinationResponse deleteResource(final DeleteDestinationRequest awsRequest,
            final ProxyClient<CloudWatchLogsClient> proxyClient) {
        DeleteDestinationResponse awsResponse = null;

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteDestination);
        } catch (CloudWatchLogsException e) {
            logger.log(String.format("Exception while deleting the %s with name %s. %s", ResourceModel.TYPE_NAME,
                    awsRequest.destinationName(), e));
            Translator.translateException(e);
        }
        logger.log(String.format("%s resource with name %s has been successfully deleted", ResourceModel.TYPE_NAME,
                awsRequest.destinationName()));
        return awsResponse;
    }

}
