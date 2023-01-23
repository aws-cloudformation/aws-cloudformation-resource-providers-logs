package software.amazon.logs.metricfilter;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterResponse;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();
        final ResourceModel previousModel = request.getPreviousResourceState();

        this.logger.log(String.format("Trying to update model %s", model.getPrimaryIdentifier()));

        return proxy.initiate("AWS-Logs-MetricFilter::Update", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToUpdateRequest)
                .backoffDelay(backoffStrategy)
                .makeServiceCall((r, c) -> updateResource(model, r, c))
                .handleError(handleError)
                .success();
    }

    private PutMetricFilterResponse updateResource(
            final ResourceModel model,
            final PutMetricFilterRequest awsRequest,
            final ProxyClient<CloudWatchLogsClient> proxyClient) {
        PutMetricFilterResponse awsResponse = null;
        try {
            boolean exists = exists(proxyClient, model);
            if (!exists) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
            }
            logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::putMetricFilter);
        } catch (final CloudWatchLogsException e) {
            Translator.translateException(e);
        }
        return awsResponse;
    }
}
