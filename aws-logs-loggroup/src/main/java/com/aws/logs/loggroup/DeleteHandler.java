package com.aws.logs.loggroup;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

import java.util.Objects;

public class DeleteHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        try {
            proxy.injectCredentialsAndInvokeV2(Translator.translateToDeleteRequest(model),
                ClientBuilder.getClient()::deleteLogGroup);
        } catch (final ResourceNotFoundException e) {
            final com.amazonaws.cloudformation.exceptions.ResourceNotFoundException rpdkException =
                new com.amazonaws.cloudformation.exceptions.ResourceNotFoundException(ResourceModel.TYPE_NAME,
                Objects.toString(model.getPrimaryIdentifier()));
            logger.log(rpdkException.getMessage());
            throw rpdkException;
        }

        final String message = String.format("%s [%s] successfully deleted.",
                ResourceModel.TYPE_NAME, model.getLogGroupName());
        logger.log(message);
        return ProgressEvent.defaultSuccessHandler(null);
    }
}
