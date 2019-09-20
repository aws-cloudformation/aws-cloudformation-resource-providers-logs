package com.amazonaws.logs.logstream;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandler<CallbackContext> {
    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ProgressEvent<ResourceModel, CallbackContext> readEvent = new ReadHandler()
            .handleRequest(proxy, request, callbackContext, logger);

        if (readEvent.isSuccess()) {
            return ProgressEvent.defaultSuccessHandler(readEvent.getResourceModel());
        }

        // Currently, no attributes are updatable for LogStream, so there's no way to
        // distinguish between a request to change a property value and a request that
        // references a non-existent primary ID.
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .status(OperationStatus.FAILED)
            .errorCode(HandlerErrorCode.NotFound)
            .build();
    }
}
