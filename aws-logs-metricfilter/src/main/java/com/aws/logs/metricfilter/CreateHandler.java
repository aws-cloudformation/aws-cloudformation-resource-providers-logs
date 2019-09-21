package com.aws.logs.metricfilter;

import com.amazonaws.cloudformation.exceptions.ResourceAlreadyExistsException;
import com.amazonaws.cloudformation.exceptions.ResourceNotFoundException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import com.amazonaws.cloudformation.resource.IdentifierUtils;
import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

import java.util.Objects;

public class CreateHandler extends BaseHandler<CallbackContext> {

    private static final int MAX_LENGTH_METRIC_FILTER_NAME = 512;

    private AmazonWebServicesClientProxy proxy;
    private ResourceHandlerRequest<ResourceModel> request;
    private CloudWatchLogsClient client;
    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        this.proxy = proxy;
        this.request = request;
        this.client = ClientBuilder.getClient();
        this.logger = logger;

        return createMetricFilter();
    }

    private ProgressEvent<ResourceModel, CallbackContext> createMetricFilter() {

        ResourceModel model = request.getDesiredResourceState();

        // resource can auto-generate a name if not supplied by caller
        // this logic should move up into the CloudFormation engine, but
        // currently exists here for backwards-compatibility with existing models
        if (StringUtils.isNullOrEmpty(model.getFilterName())) {
            model.setFilterName(
                IdentifierUtils.generateResourceIdentifier(
                    request.getLogicalResourceIdentifier(),
                    request.getClientRequestToken(),
                    MAX_LENGTH_METRIC_FILTER_NAME
                )
            );
        }

        try {
            new ReadHandler().handleRequest(proxy, request, null, logger);
            throw new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME,
                Objects.toString(model.getPrimaryIdentifier()));
        } catch (final ResourceNotFoundException e) {
            // This means a resource by this ID does not exist, which is what
            // we expect.
        }

        proxy.injectCredentialsAndInvokeV2(Translator.translateToPutRequest(model),
            client::putMetricFilter);
        logger.log(String.format("%s [%s] created successfully",
            ResourceModel.TYPE_NAME, Objects.toString(model.getPrimaryIdentifier())));

        return ProgressEvent.defaultSuccessHandler(model);
    }
}
