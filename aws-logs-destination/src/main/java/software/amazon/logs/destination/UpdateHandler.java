package software.amazon.logs.destination;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {

    private static final String DESTINATION_UPDATE_GRAPH = "AWS-Logs-Destination::Update";

    private static final String DESTINATION_POLICY_UPDATE_GRAPH = "AWS-Logs-DestinationPolicy::Update";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(final AmazonWebServicesClientProxy proxy,
                                                                          final ResourceHandlerRequest<ResourceModel> request,
                                                                          final CallbackContext callbackContext,
                                                                          final ProxyClient<CloudWatchLogsClient> proxyClient,
                                                                          final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();
        final String stackId = request.getStackId();

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> preCreateCheck(proxy, callbackContext, proxyClient, model).done(response -> {
                    if (!destinationNameExists(response, model)) {
                        return ProgressEvent.defaultFailureHandler(
                                new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString()),
                                HandlerErrorCode.NotFound
                        );
                    }
                    return ProgressEvent.progress(model, callbackContext);
                }))
                .then(progress -> putDestination(proxy, callbackContext, proxyClient, model, stackId, DESTINATION_UPDATE_GRAPH,
                        logger))
                .then(progress -> putDestinationPolicy(proxy, callbackContext, proxyClient, model,
                        DESTINATION_POLICY_UPDATE_GRAPH, logger, Action.UPDATE))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient,
                        logger));
    }
}
