package software.amazon.logs.metricfilter;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotUpdatableException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final ResourceModel previousModel = request.getPreviousResourceState();

        logger.log(String.format("Trying to update model %s", model.getPrimaryIdentifier()));

        boolean isUpdatable = isUpdatable(model, previousModel);
        if (!isUpdatable) {
            BaseHandlerException error = new CfnNotUpdatableException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
            return ProgressEvent.defaultFailureHandler(error, Translator.translateToErrorCode(error));
        }

        boolean exists = exists(proxyClient, model);
        if (!exists) {
            BaseHandlerException error = new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
            return ProgressEvent.defaultFailureHandler(error, Translator.translateToErrorCode(error));
        }

        PutMetricFilterRequest updateRequest = Translator.translateToUpdateRequest(model);
        try {
            proxyClient.injectCredentialsAndInvokeV2(updateRequest, proxyClient.client()::putMetricFilter);
        } catch (final AwsServiceException e) {
            BaseHandlerException error = Translator.translateException(e);
            return ProgressEvent.defaultFailureHandler(error, Translator.translateToErrorCode(error));
        }

        logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
        return ProgressEvent.defaultSuccessHandler(model);
    }

    private boolean isUpdatable(final ResourceModel model, final ResourceModel previousModel) {
        // An update request MUST return a NotUpdatable error if the user attempts to change a property
        // that is defined as create-only in the resource provider schema.
        if (previousModel != null) {
            return previousModel.getFilterName().equals(model.getFilterName())
                    && previousModel.getLogGroupName().equals(model.getLogGroupName());

        }
        return true;
    }
}
