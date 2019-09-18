package com.amazonaws.logs.logstream;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

import java.util.Objects;

public class ListHandler extends BaseHandler<CallbackContext> {
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

        return listLogStreams();
    }

    private ProgressEvent<ResourceModel, CallbackContext> listLogStreams() {
        final ResourceModel model = request.getDesiredResourceState();
        DescribeLogStreamsResponse response;
        try {
            response = proxy.injectCredentialsAndInvokeV2(Translator.translateToListRequest(model, request.getNextToken()),
                ClientBuilder.getClient()::describeLogStreams);
        } catch (final ResourceNotFoundException e) {
            return notFoundProgressEvent();
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .status(OperationStatus.SUCCESS)
            .resourceModels(Translator.translateForList(response, model.getLogGroupName()))
            .nextToken(response.nextToken())
            .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> notFoundProgressEvent() {
        final ResourceModel model = request.getDesiredResourceState();
        final String groupName = Objects.toString(model.getLogGroupName());
        final String errorMessage =
            Translator.buildResourceDoesNotExistErrorMessage(groupName);
        logger.log(errorMessage);
        return ProgressEvent.failed(null, null, HandlerErrorCode.NotFound, errorMessage);
    }
}
