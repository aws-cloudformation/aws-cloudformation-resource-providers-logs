package software.amazon.logs.destination;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {

    private static final String DESTINATION_UPDATE_GRAPH = "AWS-Logs-Destination::Update";
    private static final String DESTINATION_POLICY_UPDATE_GRAPH = "AWS-Logs-DestinationPolicy::Update";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request, final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient, final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final ResourceModel previousModel = request.getPreviousResourceState();

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> {
                    if (! isUpdatable(model, previousModel)) {
                        return ProgressEvent.<ResourceModel, CallbackContext>builder().errorCode(
                                HandlerErrorCode.NotUpdatable)
                                .status(OperationStatus.FAILED)
                                .build();
                    }
                    return progress;
                })
                .then(progress -> preCreateCheck(proxy, callbackContext, proxyClient, model).done((response) -> {
                    if (isDestinationListNullOrEmpty(response)) {
                        return ProgressEvent.defaultFailureHandler(new CfnNotFoundException(null),
                                HandlerErrorCode.NotFound);
                    }
                    return ProgressEvent.progress(model, callbackContext);
                }))
                .then(progress -> putDestination(proxy, callbackContext, proxyClient, model, DESTINATION_UPDATE_GRAPH,
                        logger, Action.UPDATE))
                .then(progress -> putDestinationPolicy(proxy, callbackContext, proxyClient, model,
                        DESTINATION_POLICY_UPDATE_GRAPH, logger, Action.UPDATE))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient,
                        logger));
    }

    private boolean isUpdatable(final ResourceModel model, final ResourceModel previousModel) {
        // An update request MUST return a NotUpdatable error if the user attempts to change a property
        // that is defined as create-only in the resource provider schema.
        if (previousModel != null) {
            return previousModel.getDestinationName()
                    .equals(model.getDestinationName());
        }
        return true;
    }

}
