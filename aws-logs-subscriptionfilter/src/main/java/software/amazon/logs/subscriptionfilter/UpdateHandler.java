package software.amazon.logs.subscriptionfilter;

import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.core.exception.RetryableException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.HandlerErrorCode;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;
    private static final String CALL_GRAPH_STRING = "AWS-Logs-SubscriptionFilter::Update";
    private final ReadHandler readHandler;

    public UpdateHandler() {
        super();
        readHandler = new ReadHandler();
    }

    @VisibleForTesting
    protected UpdateHandler(CloudWatchLogsClient cloudWatchLogsClient, ReadHandler readHandler) {
        super(cloudWatchLogsClient);
        this.readHandler = readHandler;
    }

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> {
                    try {
                        return readHandler.handleRequest(proxy, request, callbackContext, proxyClient, logger);
                    } catch (CfnNotFoundException e) {
                        return ProgressEvent.defaultFailureHandler(e, HandlerErrorCode.NotFound);
                    }
                })
                .onSuccess(progress ->
                        proxy.initiate(CALL_GRAPH_STRING, proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToUpdateRequest)
                                .makeServiceCall((putSubscriptionFilterRequest, client) -> client
                                        .injectCredentialsAndInvokeV2(putSubscriptionFilterRequest,
                                                client.client()::putSubscriptionFilter))
                                .handleError((cloudWatchLogsRequest, e, proxyClient1, model1, context) -> {
                                    if (shouldThrowRetryException(e)) {
                                        throw RetryableException.builder().cause(e).build();
                                    }

                                    final HandlerErrorCode handlerErrorCode = getExceptionDetails(e, logger, request.getStackId());
                                    return ProgressEvent.defaultFailureHandler(e, handlerErrorCode);
                                })
                                .progress()
                )
                .then(progress -> readHandler
                        .handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }
}
