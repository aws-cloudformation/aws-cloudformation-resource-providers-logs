package com.aws.logs.metricfilter;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;

import java.util.ArrayList;
import java.util.List;

import static com.aws.logs.metricfilter.Translator.translateFromSDK;

public class ListHandler extends BaseHandler<CallbackContext> {

    private AmazonWebServicesClientProxy proxy;
    private CloudWatchLogsClient client;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {
        this.proxy = proxy;
        this.client = ClientBuilder.getClient();

        final List<ResourceModel> models = listMetricFilters(request.getNextToken());

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(models)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    private List<ResourceModel> listMetricFilters(final String nextToken) {
        final DescribeMetricFiltersRequest request = DescribeMetricFiltersRequest.builder()
            .limit(50)
            .nextToken(nextToken)
            .build();

        DescribeMetricFiltersResponse response =
            this.proxy.injectCredentialsAndInvokeV2(request, this.client::describeMetricFilters);

        List<ResourceModel> models = new ArrayList<>();
        response.metricFilters().forEach(f -> {
            ResourceModel model = ResourceModel.builder()
                .filterName(f.filterName())
                .filterPattern(f.filterPattern())
                .logGroupName(f.logGroupName())
                .metricTransformations(translateFromSDK(f.metricTransformations()))
                .build();
            models.add(model);
        });

        return models;
    }

}
