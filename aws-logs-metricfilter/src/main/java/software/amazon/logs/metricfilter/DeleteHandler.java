package software.amazon.logs.metricfilter;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteMetricFilterResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        this.logger.log(String.format("Trying to delete model %s", model.getPrimaryIdentifier()));

        return proxy.initiate("AWS-Logs-MetricFilter::Delete", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToDeleteRequest)
                .backoffDelay(backoffStrategy)
                .makeServiceCall(this::deleteResource)
                .handleError(handleError)
                .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.SUCCESS)
                    .build());
    }

    private DeleteMetricFilterResponse deleteResource(
            final DeleteMetricFilterRequest awsRequest,
            final ProxyClient<CloudWatchLogsClient> proxyClient) {
        DeleteMetricFilterResponse awsResponse = null;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteMetricFilter);
        } catch (final CloudWatchLogsException e) {
            Translator.translateException(e);
        }

        logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }
}
