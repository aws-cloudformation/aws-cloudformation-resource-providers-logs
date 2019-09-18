package com.amazonaws.logs.destination;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

import java.util.Objects;

public class ReadHandler extends BaseHandler<CallbackContext> {
    private ResourceHandlerRequest<ResourceModel> request;
    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        this.request = request;
        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        final DescribeDestinationsResponse readResponse;
        try {
            readResponse = proxy.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model),
                ClientBuilder.getClient()::describeDestinations);
        } catch (final ResourceNotFoundException e) {
            return notFoundProgressEvent();
        }

        final ResourceModel readModel = Translator.translateForRead(readResponse);

        if (readModel == null) {
            return notFoundProgressEvent();
        }

        return ProgressEvent.defaultSuccessHandler(readModel);
    }

    private ProgressEvent<ResourceModel, CallbackContext> notFoundProgressEvent() {
        final ResourceModel model = request.getDesiredResourceState();
        final String primaryId = Objects.toString(model.getPrimaryIdentifier());
        final String errorMessage =
            Translator.buildResourceDoesNotExistErrorMessage(primaryId);
        logger.log(errorMessage);
        return ProgressEvent.failed(null, null, HandlerErrorCode.NotFound, errorMessage);
    }
}
