package com.aws.logs.loggroup;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

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
            throwNotFoundException(model);
        }


        DescribeLogGroupsResponse response = null;
        try {
            response = proxy.injectCredentialsAndInvokeV2(Translator.translateToReadRequest(model),
                ClientBuilder.getClient()::describeLogGroups);
        } catch (final ResourceNotFoundException e) {
            throwNotFoundException(model);
        }

        final ResourceModel modelFromReadResult = Translator.translateForRead(response);
        if (modelFromReadResult.getLogGroupName() == null) {
            throwNotFoundException(model);
        }

        return ProgressEvent.defaultSuccessHandler(modelFromReadResult);
    }

    private void throwNotFoundException(final ResourceModel model) {
        final ResourceModel nullSafeModel = model == null ? ResourceModel.builder().build() : model;
        final com.amazonaws.cloudformation.exceptions.ResourceNotFoundException rpdkException =
            new com.amazonaws.cloudformation.exceptions.ResourceNotFoundException(ResourceModel.TYPE_NAME,
                Objects.toString(nullSafeModel.getPrimaryIdentifier()));
        logger.log(rpdkException.getMessage());
        throw rpdkException;
    }
}
