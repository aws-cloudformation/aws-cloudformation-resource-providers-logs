package com.aws.logs.metricfilter;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

import static com.aws.logs.metricfilter.ResourceModelExtensions.getPrimaryIdentifier;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    private AmazonWebServicesClientProxy proxy;
    private CloudWatchLogsClient client;
    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        this.proxy = proxy;
        this.client = ClientBuilder.getClient();
        this.logger = logger;

        return updateMetricFilter(proxy, request);
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateMetricFilter(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request) {

        ResourceModel model = request.getDesiredResourceState();

        final ProgressEvent<ResourceModel, CallbackContext> readResult =
                new ReadHandler().handleRequest(proxy, request, null, this.logger);

        if (readResult.isFailed()) {
            return readResult;
        }

        proxy.injectCredentialsAndInvokeV2(Translator.translateToPutRequest(model),
                this.client::putMetricFilter);
        this.logger.log(String.format("%s [%s] updated successfully",
            ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString()));

        return ProgressEvent.defaultSuccessHandler(model);
    }
}
