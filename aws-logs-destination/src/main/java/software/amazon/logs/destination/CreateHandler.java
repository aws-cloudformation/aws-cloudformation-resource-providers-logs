package software.amazon.logs.destination;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {

    public static final String DESTINATION_CREATE_GRAPH = "AWS-Logs-Destination::Create";

    public static final String DESTINATION_POLICY_CREATE_GRAPH = "AWS-Logs-DestinationPolicy::Create";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final String stackId = request.getStackId();

        // Verify if a destination is already present with same identifier
        // Create destination policy command checks to see if optional destination/access policy is passed in before attempting create
        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> preCreateCheck(proxy, callbackContext, proxyClient, model).done(response -> {
                    if (destinationNameExists(response, model)) {
                        return ProgressEvent.defaultFailureHandler(
                                new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString()),
                                HandlerErrorCode.AlreadyExists
                        );
                    }
                    return ProgressEvent.progress(model, callbackContext);
                }))
                .then(progress -> putDestination(proxy, callbackContext, proxyClient, model, stackId, DESTINATION_CREATE_GRAPH,
                        logger))
                .then(progress -> model.getDestinationPolicy() != null ? putDestinationPolicy(proxy, callbackContext, proxyClient, model,
                        DESTINATION_POLICY_CREATE_GRAPH, logger, Action.CREATE) : progress)
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient,
                        logger));
    }
}
