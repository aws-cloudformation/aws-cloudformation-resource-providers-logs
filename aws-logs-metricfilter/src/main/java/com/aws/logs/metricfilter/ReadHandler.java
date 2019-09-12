package com.aws.logs.metricfilter;

import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;

import static com.aws.logs.metricfilter.ResourceModelExtensions.getPrimaryIdentifier;

public class ReadHandler extends BaseHandler<CallbackContext> {

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
        this.client = CloudWatchLogsClient.builder().build();
        this.logger = logger;

        return describeMetricFilter(request.getDesiredResourceState());
    }

    private ProgressEvent<ResourceModel, CallbackContext> describeMetricFilter(final ResourceModel model) {
        final DescribeMetricFiltersResponse response;
        try {
            response = this.proxy.injectCredentialsAndInvokeV2(Translator.translateToDescribeRequest(model, 1),
                    this.client::describeMetricFilters);
        } catch (final software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException e) {
            this.logger.log(String.format("%s [%s] doesn't exist (%s)",
                ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString(), e.getMessage()));
            return ProgressEvent.defaultFailureHandler(e, HandlerErrorCode.NotFound);
        }

        if (response.metricFilters().isEmpty()) {
            return ProgressEvent.defaultFailureHandler(new ResourceNotFoundException(ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString()),
                    HandlerErrorCode.NotFound);
        }

        return response.metricFilters()
                .stream()
                .filter(f -> getPrimaryIdentifier(Translator.translate(f)).similar(getPrimaryIdentifier(model)))
                .findFirst()
                .map(f -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .status(OperationStatus.SUCCESS)
                        .resourceModel(Translator.translate(f))
                        .build())
                .orElseGet(() -> {
                    this.logger.log(String.format("%s [%s] not found",
                            ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString()));
                    return ProgressEvent.defaultFailureHandler(new ResourceNotFoundException(ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString()),
                            HandlerErrorCode.NotFound);
                });
    }
}
