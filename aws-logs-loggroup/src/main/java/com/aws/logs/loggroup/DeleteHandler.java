package com.aws.logs.loggroup;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
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

        return deleteLogGroupIfExists();
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteLogGroupIfExists() {
        final ProgressEvent<ResourceModel, CallbackContext> readResult =
                new ReadHandler().handleRequest(proxy, request, callbackContext, logger);

        if (readResult.isFailed()) {
            return readResult;
        }

        final ResourceModel model = request.getDesiredResourceState();
        proxy.injectCredentialsAndInvokeV2(Translator.translateToDeleteRequest(model),
                ClientBuilder.getClient()::deleteLogGroup);

        final String message = String.format("%s [%s] successfully deleted.",
                ResourceModel.TYPE_NAME, model.getLogGroupName());
        logger.log(message);
        return ProgressEvent.defaultSuccessHandler(null);
    }
}
