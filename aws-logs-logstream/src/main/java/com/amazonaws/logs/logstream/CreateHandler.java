package com.amazonaws.logs.logstream;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import com.amazonaws.cloudformation.resource.IdentifierUtils;
import com.amazonaws.util.StringUtils;

import java.util.Objects;

public class CreateHandler extends BaseHandler<CallbackContext> {
    private static final int MAX_LENGTH_LOG_STREAM_NAME = 512;

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

        return createLogStream();
    }

    private ProgressEvent<ResourceModel, CallbackContext> createLogStream() {
        final ResourceModel model = request.getDesiredResourceState();
        prepareResourceModel();

        final ReadHandler readHandler = new ReadHandler();
        final ProgressEvent<ResourceModel, CallbackContext> readResponse =
            readHandler.handleRequest(proxy, request, callbackContext, logger);

        if (readResponse.isSuccess()) {
            return alreadyExistsProgressEvent();
        }

        proxy.injectCredentialsAndInvokeV2(Translator.translateToCreateRequest(model),
            ClientBuilder.getClient()::createLogStream);
        final String createMessage = String.format("%s [%s] successfully created.",
            ResourceModel.TYPE_NAME, model.getLogGroupName());
        logger.log(createMessage);

        final ResourceModel finalModel =
            readHandler.handleRequest(proxy, request, callbackContext, logger)
                .getResourceModel();

        return ProgressEvent.defaultSuccessHandler(finalModel);
    }


    private ProgressEvent<ResourceModel, CallbackContext> alreadyExistsProgressEvent() {
        final ResourceModel model = request.getDesiredResourceState();
        final String primaryId = Objects.toString(model.getPrimaryIdentifier());
        final String errorMessage =
            Translator.buildResourceAlreadyExistsErrorMessage(primaryId);
        logger.log(errorMessage);
        return ProgressEvent.failed(null, null, HandlerErrorCode.AlreadyExists, errorMessage);
    }

    private void prepareResourceModel() {
        final ResourceModel model = request.getDesiredResourceState();
        final String logicalResourceId = request.getLogicalResourceIdentifier() == null ?
            "" :
            request.getLogicalResourceIdentifier();

        if (StringUtils.isNullOrEmpty(model.getLogStreamName())) {
            model.setLogStreamName(
                IdentifierUtils.generateResourceIdentifier(
                    logicalResourceId,
                    request.getClientRequestToken(),
                    MAX_LENGTH_LOG_STREAM_NAME
                )
            );
        }
    }
}
