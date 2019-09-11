package com.aws.logs.metricfilter;

import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;

import static com.aws.logs.metricfilter.ResourceModelExtensions.getPrimaryIdentifier;
import static com.aws.logs.metricfilter.Translator.translateFromSDK;

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
        final DescribeMetricFiltersRequest request = DescribeMetricFiltersRequest.builder()
            .filterNamePrefix(model.getFilterName())
            .logGroupName(model.getLogGroupName())
            .limit(1)
            .build();

        final DescribeMetricFiltersResponse response;
        try {
            response = this.proxy.injectCredentialsAndInvokeV2(request, this.client::describeMetricFilters);
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
                .filter(f -> {
                    ResourceModel translated = ResourceModel.builder()
                            .filterName(f.filterName())
                            .filterPattern(f.filterPattern())
                            .logGroupName(f.logGroupName())
                            .metricTransformations(translateFromSDK(f.metricTransformations()))
                            .build();
                    return getPrimaryIdentifier(translated).similar(getPrimaryIdentifier(model));
                })
                .findFirst()
                .map(f -> {
                    // The API returns null when a filter pattern is "", but this is a meaningful pattern and the
                    // contract should be identical to what our caller provided
                    // per https://w.amazon.com/index.php/AWS21/Design/Uluru/HandlerContract
                    final Boolean filterPatternErased = f.filterPattern() == null && model.getFilterPattern() != null;
                    final String filterPattern = filterPatternErased ? model.getFilterPattern() : f.filterPattern();
                    return ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .status(OperationStatus.SUCCESS)
                            .resourceModel(ResourceModel.builder()
                                    .filterName(f.filterName())
                                    .filterPattern(filterPattern)
                                    .logGroupName(f.logGroupName())
                                    .metricTransformations(translateFromSDK(f.metricTransformations()))
                                    .build())
                            .build();
                }).orElseGet(() -> {
                    this.logger.log(String.format("%s [%s] not found",
                            ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString()));
                    return ProgressEvent.defaultFailureHandler(new ResourceNotFoundException(ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString()),
                            HandlerErrorCode.NotFound);
                });
    }
}
