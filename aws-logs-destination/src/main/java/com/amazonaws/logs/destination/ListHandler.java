package com.amazonaws.logs.destination;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;

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

        final DescribeDestinationsResponse response =
            proxy.injectCredentialsAndInvokeV2(Translator.translateToListRequest(request.getNextToken()),
                ClientBuilder.getClient()::describeDestinations);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(Translator.translateForList(response))
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
