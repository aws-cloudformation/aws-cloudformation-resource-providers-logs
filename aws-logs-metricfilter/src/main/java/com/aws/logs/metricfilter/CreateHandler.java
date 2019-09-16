package com.aws.logs.metricfilter;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
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

        // pre-creation read to ensure no existing resource exists
        final ProgressEvent<ResourceModel, CallbackContext> readResult =
            new ReadHandler().handleRequest(proxy, request, null, logger);
        final String primaryId = Objects.toString(model.getPrimaryIdentifier());
        if (readResult.isSuccess()) {
            final String errorMessage = Translator.buildResourceAlreadyExistsErrorMessage(primaryId);
            logger.log(errorMessage);
            return ProgressEvent.failed(null, null, HandlerErrorCode.AlreadyExists, errorMessage);
        }

        proxy.injectCredentialsAndInvokeV2(Translator.translateToPutRequest(model),
            client::putMetricFilter);
        logger.log(String.format("%s [%s] created successfully",
            ResourceModel.TYPE_NAME, primaryId));

        return ProgressEvent.defaultSuccessHandler(model);
    }
}
