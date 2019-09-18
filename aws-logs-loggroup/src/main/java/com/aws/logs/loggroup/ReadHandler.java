package com.aws.logs.loggroup;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;

import java.util.Objects;

public class ReadHandler extends BaseHandler<CallbackContext> {

    private AmazonWebServicesClientProxy proxy;
    private ResourceHandlerRequest<ResourceModel> request;
    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        this.proxy = proxy;
        this.request = request;
        this.logger = logger;

        return fetchLogGroupAndAssertExists();
    }

    private ProgressEvent<ResourceModel, CallbackContext> fetchLogGroupAndAssertExists() {
        final ResourceModel model = request.getDesiredResourceState();

        if (model == null || model.getLogGroupName() == null) {
            return notFoundProgressEvent();
        }

        final DescribeLogGroupsResponse response =
                proxy.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model),
                        ClientBuilder.getClient()::describeLogGroups);
        final ResourceModel modelFromReadResult = Translator.translateForRead(response);

        if (modelFromReadResult.getLogGroupName() == null) {
            return notFoundProgressEvent();
        }

        return ProgressEvent.defaultSuccessHandler(modelFromReadResult);
    }

    private ProgressEvent<ResourceModel, CallbackContext> notFoundProgressEvent() {
        final ResourceModel model = request.getDesiredResourceState();
        final String primaryId = model == null ? null : Objects.toString(model.getPrimaryIdentifier());
        final String errorMessage =
            Translator.buildResourceDoesNotExistErrorMessage(primaryId);
        logger.log(errorMessage);
        return ProgressEvent.failed(null, null, HandlerErrorCode.NotFound, errorMessage);
    }
}
