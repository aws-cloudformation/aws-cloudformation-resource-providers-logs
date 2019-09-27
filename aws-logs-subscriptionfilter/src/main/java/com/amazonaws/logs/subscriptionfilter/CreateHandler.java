package com.amazonaws.logs.subscriptionfilter;

import com.amazonaws.cloudformation.exceptions.ResourceAlreadyExistsException;
import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import com.amazonaws.cloudformation.resource.IdentifierUtils;

import java.util.Objects;

public class CreateHandler extends BaseHandler<CallbackContext> {
    private static final String DEFAULT_FILTER_NAME_PREFIX = "SubscriptionFilter";
    private static final int MAX_LENGTH_LOG_STREAM_NAME = 512;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final String logicalResourceId = request.getLogicalResourceIdentifier() == null ?
            DEFAULT_FILTER_NAME_PREFIX :
            request.getLogicalResourceIdentifier();

        final String filterName = IdentifierUtils.generateResourceIdentifier(
            logicalResourceId,
            request.getClientRequestToken(),
            MAX_LENGTH_LOG_STREAM_NAME
        );

        request.getDesiredResourceState().setFilterName(filterName);

        final ResourceModel model = request.getDesiredResourceState();

        try {
            new ReadHandler().handleRequest(proxy, request, callbackContext, logger);
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME,
                Objects.toString(model.getPrimaryIdentifier()));
        } catch (final ResourceNotFoundException e) {
            proxy.injectCredentialsAndInvokeV2(Translator.requestForPut(model),
                ClientBuilder.getClient()::putSubscriptionFilter);
            logger.log(String.format("%s [%s] created successfully",
                ResourceModel.TYPE_NAME, Objects.toString(model.getPrimaryIdentifier())));

            return ProgressEvent.defaultSuccessHandler(model);
        }
    }
}
