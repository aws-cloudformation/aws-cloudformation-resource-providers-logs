package com.amazonaws.logs.destination;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        new ReadHandler().handleRequest(proxy, request, callbackContext, logger);

        proxy.injectCredentialsAndInvokeV2(Translator.translateToDeleteRequest(request.getDesiredResourceState()),
            ClientBuilder.getClient()::deleteDestination);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
