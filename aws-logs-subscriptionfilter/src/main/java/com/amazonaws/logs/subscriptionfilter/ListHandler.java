package com.amazonaws.logs.subscriptionfilter;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

import java.util.Objects;

public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        DescribeSubscriptionFiltersResponse listResponse;
        try {
            listResponse = proxy.injectCredentialsAndInvokeV2(Translator.requestForList(model, request.getNextToken()),
                ClientBuilder.getClient()::describeSubscriptionFilters);
        } catch (final ResourceNotFoundException e) {
            throw new com.amazonaws.cloudformation.exceptions.ResourceNotFoundException(ResourceModel.TYPE_NAME,
                Objects.toString(model.getPrimaryIdentifier()),
                e);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .status(OperationStatus.SUCCESS)
            .nextToken(listResponse.nextToken())
            .resourceModels(Translator.translateForList(listResponse))
            .build();
    }
}
