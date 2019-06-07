package com.aws.logs.metricfilter;

import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.MetricFilter;

import java.util.Optional;

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

        final ResourceModel model = describeMetricFilter(request.getDesiredResourceState());

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    private ResourceModel describeMetricFilter(final ResourceModel model) {
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
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString());
        }

        if (response.metricFilters().size() == 0) {
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString());
        }

        final Optional<MetricFilter> result = response.metricFilters().stream().filter(f -> {
            ResourceModel translated = ResourceModel.builder()
                .filterName(f.filterName())
                .filterPattern(f.filterPattern())
                .logGroupName(f.logGroupName())
                .metricTransformations(translateFromSDK(f.metricTransformations()))
                .build();
            return getPrimaryIdentifier(translated).similar(getPrimaryIdentifier(model));
        }).findFirst();

        if (!result.isPresent()) {
            this.logger.log(String.format("%s [%s] not found",
                ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString()));
            throw new ResourceNotFoundException(ResourceModel.TYPE_NAME, model.getFilterName());
        }

        final MetricFilter f = result.get();
        return ResourceModel.builder()
            .filterName(f.filterName())
            .filterPattern(f.filterPattern())
            .logGroupName(f.logGroupName())
            .metricTransformations(translateFromSDK(f.metricTransformations()))
            .build();
    }
}
