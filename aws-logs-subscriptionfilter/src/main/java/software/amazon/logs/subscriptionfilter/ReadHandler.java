package software.amazon.logs.subscriptionfilter;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.proxy.*;

public class ReadHandler extends BaseHandlerStd {
    private Logger logger;
    private static final String CALL_GRAPH_STRING = "AWS-Logs-SubscriptionFilter::Read";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();
        final String stackId = request.getStackId() == null ? "" : request.getStackId();

        logger.log(String.format("Invoking request for: %s for stack: %s", CALL_GRAPH_STRING, stackId));

        return proxy.initiate(CALL_GRAPH_STRING, proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((cloudWatchLogsRequest, sdkProxyClient) -> {
                    return sdkProxyClient.injectCredentialsAndInvokeV2(cloudWatchLogsRequest,
                            sdkProxyClient.client()::describeSubscriptionFilters);
                }).handleError((cloudWatchLogsRequest, e, _proxyClient, _model, ctx) -> {
                    if (isAccessDeniedError(e, logger)) {
                        return ProgressEvent.success(model, ctx);
                    } else {
                        final HandlerErrorCode handlerErrorCode = getExceptionDetails(e, logger, stackId);
                        return ProgressEvent.failed(model, callbackContext, handlerErrorCode, e.getMessage());
                    }
                })
                .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .status(OperationStatus.SUCCESS)
                        .resourceModel(Translator.translateFromReadResponse(awsResponse))
                        .build());
    }

}
