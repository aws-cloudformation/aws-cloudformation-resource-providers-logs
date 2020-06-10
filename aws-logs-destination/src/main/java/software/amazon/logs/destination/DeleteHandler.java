package software.amazon.logs.destination;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteDestinationRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteDestinationResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request, final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient, final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        ProgressEvent<ResourceModel, CallbackContext> progressEvent = ProgressEvent.progress(model, callbackContext)
                .then(progress -> proxy.initiate("AWS-Logs-Destination::Delete", proxyClient, model, callbackContext)
                        .translateToServiceRequest(Translator::translateToDeleteRequest)
                        .makeServiceCall(this::deleteResource)
                        .success());

        logger.log(String.format("%s resource with name %s has been successfully deleted", ResourceModel.TYPE_NAME,
                model.getDestinationName()));

        return progressEvent;
    }

    private DeleteDestinationResponse deleteResource(final DeleteDestinationRequest awsRequest,
            final ProxyClient<CloudWatchLogsClient> proxyClient) {

        DeleteDestinationResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteDestination);
        } catch (AwsServiceException e) {
            Translator.translateException(e);
        }
        return awsResponse;
    }

}
