package com.aws.logs.loggroup;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;

public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final DescribeLogGroupsResponse response =
                proxy.injectCredentialsAndInvokeV2(Translator.translateToListRequest(request.getNextToken()),
                    ClientBuilder.getClient()::describeLogGroups);
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .status(OperationStatus.SUCCESS)
                .resourceModels(Translator.translateForList(response))
                .nextToken(response.nextToken())
                .build();
    }
}
