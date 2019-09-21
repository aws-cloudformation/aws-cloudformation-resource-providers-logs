package com.amazonaws.logs.destination;

import com.amazonaws.cloudformation.exceptions.ResourceAlreadyExistsException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

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

        try {
            new ReadHandler().handleRequest(proxy, request, null, logger);
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME,
                Objects.toString(request.getDesiredResourceState().getPrimaryIdentifier()));
        } catch (final ResourceNotFoundException e) {
            // This means a resource by this ID does not exist, which is what
            // we expect.
        }

        return HandlerHelper.putDestination(proxy, request, callbackContext, logger);
    }
}
