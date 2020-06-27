package software.amazon.logs.metricfilter;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteMetricFilterRequest;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final DeleteMetricFilterRequest deleteMetricFilterRequest = Translator.translateToDeleteRequest(model);

        try {
            proxyClient.injectCredentialsAndInvokeV2(deleteMetricFilterRequest, proxyClient.client()::deleteMetricFilter);
        } catch (final AwsServiceException e) {
            BaseHandlerException error = Translator.translateException(e);
            return ProgressEvent.defaultFailureHandler(error, Translator.translateToErrorCode(error));
        }
        logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
        return ProgressEvent.defaultSuccessHandler(model);
    }
}
