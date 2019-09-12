package com.aws.logs.loggroup;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutRetentionPolicyRequest;

import java.util.Optional;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    private AmazonWebServicesClientProxy proxy;
    private ResourceHandlerRequest<ResourceModel> request;
    private CallbackContext callbackContext;
    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        this.proxy = proxy;
        this.request = request;
        this.callbackContext = callbackContext;
        this.logger = logger;

        // RetentionPolicyInDays is the only attribute that is not createOnly
        return updateRetentionPolicy();
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateRetentionPolicy() {
        final ProgressEvent<ResourceModel, CallbackContext> readResult =
                new ReadHandler().handleRequest(proxy, request, callbackContext, logger);
        if (readResult.isFailed()) {
            return readResult;
        }

        final ResourceModel model = request.getDesiredResourceState();

        final boolean retentionInDaysShouldBeUpdated =
            Optional.ofNullable(model.getRetentionInDays())
                .map(requestedRetention -> !requestedRetention.equals(readResult.getResourceModel().getRetentionInDays()))
                .orElseGet(() -> readResult.getResourceModel().getRetentionInDays() != null);

        if (retentionInDaysShouldBeUpdated) {
            if (model.getRetentionInDays() == null) {
                deleteRetentionPolicy();
            } else {
                putRetentionPolicy();
            }
        }

        return new ReadHandler().handleRequest(proxy, request, callbackContext, logger);
    }

    private void deleteRetentionPolicy() {
        final ResourceModel model = request.getDesiredResourceState();
        final DeleteRetentionPolicyRequest deleteRetentionPolicyRequest =
            Translator.translateToDeleteRetentionPolicyRequest(model);
        proxy.injectCredentialsAndInvokeV2(deleteRetentionPolicyRequest,
            ClientBuilder.getClient()::deleteRetentionPolicy);

        final String retentionPolicyMessage =
            String.format("%s [%s] successfully deleted retention policy.",
                ResourceModel.TYPE_NAME, model.getLogGroupName());
        logger.log(retentionPolicyMessage);
    }

    private void putRetentionPolicy() {
        final ResourceModel model = request.getDesiredResourceState();
        final PutRetentionPolicyRequest putRetentionPolicyRequest =
            Translator.translateToPutRetentionPolicyRequest(model);
        proxy.injectCredentialsAndInvokeV2(putRetentionPolicyRequest,
            ClientBuilder.getClient()::putRetentionPolicy);

        final String retentionPolicyMessage =
            String.format("%s [%s] successfully applied retention in days: [%d].",
                ResourceModel.TYPE_NAME, model.getLogGroupName(), model.getRetentionInDays());
        logger.log(retentionPolicyMessage);
    }
}
