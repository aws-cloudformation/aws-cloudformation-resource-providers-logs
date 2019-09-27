package com.amazonaws.logs.subscriptionfilter;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

import java.util.Objects;

public class ReadHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        DescribeSubscriptionFiltersResponse readResponse;
        try {
            readResponse = proxy.injectCredentialsAndInvokeV2(Translator.requestForRead(model),
                ClientBuilder.getClient()::describeSubscriptionFilters);
        } catch (final ResourceNotFoundException e) {
            throw new com.amazonaws.cloudformation.exceptions.ResourceNotFoundException(ResourceModel.TYPE_NAME,
                Objects.toString(model.getPrimaryIdentifier()));
        }

        return Translator.translateForRead(readResponse)
            .map(ProgressEvent::<ResourceModel, CallbackContext>defaultSuccessHandler)
            .orElseThrow(() ->
                new com.amazonaws.cloudformation.exceptions.ResourceNotFoundException(ResourceModel.TYPE_NAME,
                    Objects.toString(model.getPrimaryIdentifier())));
    }
}
