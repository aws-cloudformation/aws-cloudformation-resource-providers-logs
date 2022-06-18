package software.amazon.logs.logstream;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.proxy.*;

import java.util.List;

import java.util.ArrayList;

import com.amazonaws.util.StringUtils;

public class ListHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final String stackId = request.getStackId() == null ? "" : request.getStackId();
        final String nextToken = request.getNextToken();

        logger.log(String.format("Invoking request for: %s with StackID: %s", "AWS-Logs-LogStream::List", stackId));

        // if log group name is null then return an error message
        if (model == null || StringUtils.isNullOrEmpty(model.getLogGroupName())){
            return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.InvalidRequest, "Log Group Name cannot be empty");
        }

        return proxy.initiate("AWS-Logs-LogStream::List", proxyClient, model, callbackContext)
                .translateToServiceRequest((cbModel) -> Translator.translateToListRequest(cbModel, nextToken))
                .makeServiceCall((listFiltersRequest, _proxyClient) -> _proxyClient.injectCredentialsAndInvokeV2(listFiltersRequest, _proxyClient.client()::describeLogStreams))
                .handleError((cbRequest, exception, cbProxyClient, cbModel, cbContext) -> handleError(cbRequest, exception, cbProxyClient, cbModel, cbContext))
                .done((describeLogStreamsRequest, describeLogStreamsResponse, client, _model, _callbackContext) -> {
                    final List<ResourceModel> modelList = Translator.translateFromListResponse(describeLogStreamsResponse, _model);
                    return ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .resourceModels(modelList)
                            .nextToken(nextToken)
                            .status(OperationStatus.SUCCESS)
                            .build();
                });

    }
}
