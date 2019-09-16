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
        this.client = CloudWatchLogsClient.builder().build();
        this.logger = logger;

        return describeMetricFilter();
    }

    private ProgressEvent<ResourceModel, CallbackContext> describeMetricFilter() {
        final ResourceModel model = request.getDesiredResourceState();
        final DescribeMetricFiltersResponse response;
        try {
            response = proxy.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model),
                    client::describeMetricFilters);
        } catch (final software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException e) {
            logger.log(String.format("%s [%s] doesn't exist (%s)",
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
                    final String primaryId = getPrimaryIdentifier(model).toString();
                    final String errorMessage = Translator.buildResourceDoesNotExistErrorMessage(primaryId);
                    logger.log(errorMessage);
                    return ProgressEvent.failed(null, null, HandlerErrorCode.NotFound, errorMessage);
                });
    }
}
