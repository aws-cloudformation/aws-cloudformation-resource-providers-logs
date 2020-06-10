package software.amazon.logs.destination;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {

    public static final String DESTINATION_CREATE_GRAPH = "AWS-Logs-Destination::Create";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request, final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient, final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        // Verify if a destination is already present with same identifier
        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> preCreateCheck(proxy, callbackContext, proxyClient, model).done((response) -> {
                    if (isDestinationListNullOrEmpty(response)) {
                        return ProgressEvent.progress(model, callbackContext);
                    }
                    return ProgressEvent.defaultFailureHandler(new CfnAlreadyExistsException(null),
                            HandlerErrorCode.AlreadyExists);
                }))
                .then(progress -> putDestination(proxy, callbackContext, proxyClient, model, DESTINATION_CREATE_GRAPH,
                        logger))
                .then(progress -> putDestinationPolicy(proxy, callbackContext, proxyClient, model,
                        DESTINATION_CREATE_GRAPH, logger))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient,
                        logger));
    }

}
