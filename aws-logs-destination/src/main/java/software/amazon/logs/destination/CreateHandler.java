package software.amazon.logs.destination;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;
import software.amazon.logs.common.MetricsConstants;
import software.amazon.logs.common.MetricsHelper;

public class CreateHandler extends BaseHandlerStd {

    public static final String CALL_GRAPH = "AWS-Logs-Destination::Create";

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
            .translateToServiceRequest(Translator::translateToPutDestinationRequest)
            .backoffDelay(getBackOffStrategy())
            .makeServiceCall((awsRequest, client) -> putDestination(model, awsRequest, client, logger, Action.CREATE, metrics))
            .retryErrorFilter((_request, exception, _proxyClient, _model, _context) -> isRetryableException(exception))
            .done(_createResponse ->
                ProgressEvent.<ResourceModel, CallbackContext>builder().status(OperationStatus.SUCCESS).resourceModel(model).build()
            );
    }
}
