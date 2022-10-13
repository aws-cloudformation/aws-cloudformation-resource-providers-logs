package software.amazon.logs.subscriptionfilter;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;

import java.util.List;

public class ListHandler extends BaseHandlerStd {
    private static final String CALL_GRAPH_STRING = "AWS-Logs-SubscriptionFilter::List";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final String stackId = request.getStackId() == null ? "" : request.getStackId();
        final String nextToken = request.getNextToken();

        logger.log(String.format("Invoking request for: %s with StackID: %s", CALL_GRAPH_STRING, stackId));

        return proxy.initiate(CALL_GRAPH_STRING, proxyClient, model, callbackContext)
                .translateToServiceRequest(cbModel -> Translator.translateToListRequest(cbModel, nextToken))
                .makeServiceCall((listFiltersRequest, _proxyClient) -> _proxyClient
                        .injectCredentialsAndInvokeV2(listFiltersRequest, _proxyClient.client()::describeSubscriptionFilters))
                .done((describeSubscriptionFiltersRequest, describeSubscriptionFiltersResponse, client, _model, _callbackContext) -> {
                    final List<ResourceModel> modelList = Translator.translateFromListResponse(describeSubscriptionFiltersResponse);
                    return ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .resourceModels(modelList)
                            .nextToken(nextToken)
                            .status(OperationStatus.SUCCESS)
                            .build();
                });
    }
}
