package software.amazon.logs.metricfilter;

import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;

public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final DescribeMetricFiltersRequest awsRequest = Translator.translateToListRequest(request.getNextToken());

        DescribeMetricFiltersResponse awsResponse = proxy.injectCredentialsAndInvokeV2(awsRequest, ClientBuilder.getClient()::describeMetricFilters);

        final List<ResourceModel> models = Translator.translateFromListRequest(awsResponse);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(models)
            .nextToken(awsResponse.nextToken())
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
