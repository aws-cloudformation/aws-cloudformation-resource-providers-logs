package software.amazon.logs.metricfilter;

import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

import java.util.Objects;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    private AmazonWebServicesClientProxy proxy;
    private ResourceHandlerRequest<ResourceModel> request;
    private CloudWatchLogsClient client;
    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        this.proxy = proxy;
        this.request = request;
        this.client = ClientBuilder.getClient();
        this.logger = logger;

        return updateMetricFilter();
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateMetricFilter() {

        ResourceModel previousModel = request.getPreviousResourceState();
        ResourceModel model = request.getDesiredResourceState();

        // An update request MUST return a NotUpdatable error if the user attempts to change a property
        // that is defined as create-only in the resource provider schema.
        if (previousModel != null) {
            if (!previousModel.getFilterName().equals(model.getFilterName())
            || !previousModel.getLogGroupName().equals(model.getLogGroupName()))
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .errorCode(HandlerErrorCode.NotUpdatable)
                    .status(OperationStatus.FAILED)
                    .build();
        }

        // An update request MUST return a NotFound error if the resource does not exist.
        try {
            new ReadHandler().handleRequest(proxy, request, null, logger);
        } catch (ResourceNotFoundException e) {
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME,
                    Objects.toString(model.getPrimaryIdentifier()));
        }

        proxy.injectCredentialsAndInvokeV2(Translator.translateToPutRequest(model),
            client::putMetricFilter);
        logger.log(String.format("%s [%s] updated successfully",
            ResourceModel.TYPE_NAME, Objects.toString(model.getPrimaryIdentifier())));

        return ProgressEvent.defaultSuccessHandler(model);
    }
}
