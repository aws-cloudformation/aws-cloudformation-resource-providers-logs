package com.amazonaws.logs.logstream;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandler<CallbackContext> {
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

        return deleteLogStream();
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteLogStream() {
        final ResourceModel model = request.getDesiredResourceState();
        final ReadHandler readHandler = new ReadHandler();
        readHandler.handleRequest(proxy, request, callbackContext, logger);

        proxy.injectCredentialsAndInvokeV2(Translator.translateToDeleteRequest(model),
            ClientBuilder.getClient()::deleteLogStream);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
