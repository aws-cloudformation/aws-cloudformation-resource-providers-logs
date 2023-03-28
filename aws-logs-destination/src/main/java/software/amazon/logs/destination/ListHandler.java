package software.amazon.logs.destination;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ListHandler extends BaseHandlerStd {

    private Logger logger;
    public static final String DESTINATION_POLICY_LIST_GRAPH = "AWS-Logs-Destination::List";

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {
        this.logger = logger;

        return proxy.initiate(DESTINATION_POLICY_LIST_GRAPH, proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(Translator::translateToListRequest)
                .makeServiceCall((describeDestinationsRequest, client) -> {
                    DescribeDestinationsResponse awsResponse = null;
                    try {
                        awsResponse = client.injectCredentialsAndInvokeV2(describeDestinationsRequest, proxyClient.client()::describeDestinations);
                    } catch (CloudWatchLogsException e) {
                        Translator.translateException(e);
                    }
                    return awsResponse;
                })
                .done(describeDestinationsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .resourceModels(Translator.translateFromListResponse(describeDestinationsResponse))
                        .status(OperationStatus.SUCCESS)
                        .nextToken(describeDestinationsResponse.nextToken())
                        .build());
    }
}
