package com.aws.logs.metricfilter;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;

import static com.aws.logs.metricfilter.ResourceModelExtensions.getPrimaryIdentifier;
import static com.aws.logs.metricfilter.Translator.translateToSDK;

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

        // pre-creation read to ensure resource exists (the underlying API is UPSERT style, but we want consistent
        // behaviour for the CloudFormation control plane; READ will throw if can't find the resource
        new ReadHandler().handleRequest(proxy, request, null, this.logger);

        final PutMetricFilterRequest putMetricFilterRequest =
            PutMetricFilterRequest.builder()
                .filterName(model.getFilterName())
                .filterPattern(model.getFilterPattern())
                .logGroupName(model.getLogGroupName())
                .metricTransformations(translateToSDK(model.getMetricTransformations()))
                .build();
        proxy.injectCredentialsAndInvokeV2(putMetricFilterRequest, this.client::putMetricFilter);
        this.logger.log(String.format("%s [%s] updated successfully",
            ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString()));

        return ProgressEvent.defaultSuccessHandler(model);
    }
}
