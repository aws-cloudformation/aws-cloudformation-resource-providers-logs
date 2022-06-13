package software.amazon.logs.logstream;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.proxy.*;

import java.util.List;

import java.util.ArrayList;

public class ListHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {

//        final List<ResourceModel> models = new ArrayList<>();

        final ResourceModel model = request.getDesiredResourceState();
        final String stackId = request.getStackId() == null ? "" : request.getStackId();
        final String nextToken = request.getNextToken();

        logger.log(String.format("Invoking request for: %s with StackID: %s", "AWS-Logs-LogStream::List", stackId));

        return proxy.initiate("AWS-Logs-LogStream::List", proxyClient, model, callbackContext)
                .translateToServiceRequest((cbModel) -> Translator.translateToListRequest(cbModel, nextToken))
                .makeServiceCall((listFiltersRequest, _proxyClient) -> _proxyClient
                        .injectCredentialsAndInvokeV2(listFiltersRequest, _proxyClient.client()::describeLogStreams))
                .done((describeLogStreamsRequest, describeLogStreamsResponse, client, _model, _callbackContext) -> {
                    final List<ResourceModel> modelList = Translator.translateFromListResponse(describeLogStreamsResponse);
                    return ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .resourceModels(modelList)
                            .nextToken(nextToken)
                            .status(OperationStatus.SUCCESS)
                            .build();
                });

    }
}
