package com.aws.logs.metricfilter;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

import static com.aws.logs.metricfilter.ResourceModelExtensions.getPrimaryIdentifier;

public class DeleteHandler extends BaseHandler<CallbackContext> {

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

        return deleteMetricFilter();
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteMetricFilter() {

        ResourceModel model = request.getDesiredResourceState();

        try {
            proxy.injectCredentialsAndInvokeV2(Translator.translateToDeleteRequest(model),
                    client::deleteMetricFilter);
            logger.log(String.format("%s [%s] deleted successfully",
                ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString()));
        } catch (ResourceNotFoundException e) {
            logger.log(String.format("%s [%s] is already deleted",
                ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString()));
            return ProgressEvent.defaultFailureHandler(e, HandlerErrorCode.NotFound);
        }

        return ProgressEvent.defaultSuccessHandler(null);
    }
}
