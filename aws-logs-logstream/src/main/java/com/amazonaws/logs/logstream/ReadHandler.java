package com.amazonaws.logs.logstream;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;

import java.util.Objects;

public class ReadHandler extends BaseHandler<CallbackContext> {
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

        return readLogStream();
    }

    private ProgressEvent<ResourceModel, CallbackContext> readLogStream() {
        final ResourceModel model = request.getDesiredResourceState();
        final DescribeLogStreamsResponse result =
            proxy.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model),
                ClientBuilder.getClient()::describeLogStreams);
        final ResourceModel readModel = Translator.translateForRead(result, model.getLogGroupName());
        if (readModel.getLogStreamName() == null) {
            return notFoundProgressEvent();
        }
        return ProgressEvent.defaultSuccessHandler(Translator.translateForRead(result, model.getLogGroupName()));
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
