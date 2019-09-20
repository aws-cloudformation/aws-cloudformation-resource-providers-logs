package com.amazonaws.logs.destination;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ProgressEvent<ResourceModel, CallbackContext> readResult =
            new ReadHandler().handleRequest(proxy, request, callbackContext, logger);

        if (readResult.isFailed()) {
            return readResult;
        }

        return HandlerHelper.putDestination(proxy, request, callbackContext, logger);
    }
}
