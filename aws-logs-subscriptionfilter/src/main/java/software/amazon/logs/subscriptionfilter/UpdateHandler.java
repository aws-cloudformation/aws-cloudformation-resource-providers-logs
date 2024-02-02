package software.amazon.logs.subscriptionfilter;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;
import software.amazon.logs.common.MetricsConstants;
import software.amazon.logs.common.MetricsHelper;

public class UpdateHandler extends BaseHandlerStd {

    private static final String CALL_GRAPH = "AWS-Logs-SubscriptionFilter::Update";

    public UpdateHandler() {
        super();
    }

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger,
        final MetricsLogger metrics
    ) {
        MetricsHelper.putProperty(metrics, MetricsConstants.OPERATION, CALL_GRAPH);

        final ResourceModel model = request.getDesiredResourceState();

        return proxy
            .initiate(CALL_GRAPH, proxyClient, model, callbackContext)
            .translateToServiceRequest(Translator::translateToUpdateRequest)
            .backoffDelay(getBackOffStrategy())
            .makeServiceCall((putSubscriptionFilterRequest, client) ->
                putResource(model, putSubscriptionFilterRequest, client, Action.UPDATE, logger, metrics)
            )
            .retryErrorFilter((_request, exception, _proxyClient, _model, _context) -> isRetryableException(exception))
            .success();
    }
}
