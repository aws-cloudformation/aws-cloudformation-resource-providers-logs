package software.amazon.logs.metricfilter;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;

public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final String nextToken = request.getNextToken();
        final DescribeMetricFiltersResponse response =
            proxy.injectCredentialsAndInvokeV2(Translator.translateToListRequest(50, nextToken),
                ClientBuilder.getClient()::describeMetricFilters);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(Translator.translateFromSDK(response))
            .nextToken(response.nextToken())
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
