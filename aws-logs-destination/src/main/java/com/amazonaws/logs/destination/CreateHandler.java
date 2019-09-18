package com.amazonaws.logs.destination;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Objects;

public class CreateHandler extends BaseHandler<CallbackContext> {
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

        return createDestination();
    }

    private ProgressEvent<ResourceModel, CallbackContext> createDestination() {
        final ResourceModel model = request.getDesiredResourceState();

        final ReadHandler readHandler = new ReadHandler();
        final ProgressEvent<ResourceModel, CallbackContext> readResult =
            readHandler.handleRequest(proxy, request, callbackContext, logger);

        if (readResult.isSuccess()) {
            return alreadyExistsProgressEvent();
        }

        return HandlerHelper.putDestination(proxy, request, callbackContext, logger);
    }

    private ProgressEvent<ResourceModel, CallbackContext> alreadyExistsProgressEvent() {
        final ResourceModel model = request.getDesiredResourceState();
        final String primaryId = Objects.toString(model.getPrimaryIdentifier());
        final String errorMessage =
            Translator.buildResourceAlreadyExistsErrorMessage(primaryId);
        logger.log(errorMessage);
        return ProgressEvent.failed(null, null, HandlerErrorCode.AlreadyExists, errorMessage);
    }
}
