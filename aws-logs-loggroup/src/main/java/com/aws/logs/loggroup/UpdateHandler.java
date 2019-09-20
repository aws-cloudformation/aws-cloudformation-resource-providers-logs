package com.aws.logs.loggroup;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

import java.util.Objects;

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
        final ResourceModel model = request.getDesiredResourceState();

        if (model.getRetentionInDays() == null) {
            deleteRetentionPolicy();
        } else {
            putRetentionPolicy();
        }

        return new ReadHandler().handleRequest(proxy, request, callbackContext, logger);
    }

    private void deleteRetentionPolicy() {
        final ResourceModel model = request.getDesiredResourceState();
        final DeleteRetentionPolicyRequest deleteRetentionPolicyRequest =
            Translator.translateToDeleteRetentionPolicyRequest(model);
        try {
            proxy.injectCredentialsAndInvokeV2(deleteRetentionPolicyRequest,
                ClientBuilder.getClient()::deleteRetentionPolicy);
        } catch (final ResourceNotFoundException e) {
            throwNotFoundException(model);
        }

        final String retentionPolicyMessage =
            String.format("%s [%s] successfully deleted retention policy.",
                ResourceModel.TYPE_NAME, model.getLogGroupName());
        logger.log(retentionPolicyMessage);
    }

    private void putRetentionPolicy() {
        final ResourceModel model = request.getDesiredResourceState();
        final PutRetentionPolicyRequest putRetentionPolicyRequest =
            Translator.translateToPutRetentionPolicyRequest(model);
        try {
            proxy.injectCredentialsAndInvokeV2(putRetentionPolicyRequest,
                ClientBuilder.getClient()::putRetentionPolicy);
        } catch (final ResourceNotFoundException e) {
            throwNotFoundException(model);
        }

        final String retentionPolicyMessage =
            String.format("%s [%s] successfully applied retention in days: [%d].",
                ResourceModel.TYPE_NAME, model.getLogGroupName(), model.getRetentionInDays());
        logger.log(retentionPolicyMessage);
    }

    private void throwNotFoundException(final ResourceModel model) {
        final com.amazonaws.cloudformation.exceptions.ResourceNotFoundException rpdkException =
            new com.amazonaws.cloudformation.exceptions.ResourceNotFoundException(ResourceModel.TYPE_NAME,
                Objects.toString(model.getPrimaryIdentifier()));
        logger.log(rpdkException.getMessage());
        throw rpdkException;
    }
}
