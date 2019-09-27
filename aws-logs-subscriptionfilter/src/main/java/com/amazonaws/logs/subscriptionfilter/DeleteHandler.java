package com.amazonaws.logs.subscriptionfilter;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
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

        try {
            proxy.injectCredentialsAndInvokeV2(Translator.requestForDelete(request.getDesiredResourceState()),
                ClientBuilder.getClient()::deleteSubscriptionFilter);
            logger.log(String.format("%s [%s] deleted successfully",
                ResourceModel.TYPE_NAME, Objects.toString(request.getDesiredResourceState().getPrimaryIdentifier())));
        } catch (final ResourceNotFoundException e) {
            throw new com.amazonaws.cloudformation.exceptions.ResourceNotFoundException(
                ResourceModel.TYPE_NAME,
                Objects.toString(request.getDesiredResourceState().getPrimaryIdentifier()),
                e);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
