package com.aws.logs.metricfilter;

import com.amazonaws.cloudformation.exceptions.ResourceAlreadyExistsException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import com.amazonaws.cloudformation.resource.IdentifierUtils;
import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

import static com.aws.logs.metricfilter.ResourceModelExtensions.getPrimaryIdentifier;

public class CreateHandler extends BaseHandler<CallbackContext> {

    private static final int MAX_LENGTH_METRIC_FILTER_NAME = 512;

    private AmazonWebServicesClientProxy proxy;
    private CloudWatchLogsClient client;
    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        this.proxy = proxy;
        this.client = ClientBuilder.getClient();
        this.logger = logger;

        return createMetricFilter(proxy, request);
    }

    private ProgressEvent<ResourceModel, CallbackContext> createMetricFilter(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request) {

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
                new ReadHandler().handleRequest(proxy, request, null, this.logger);
        final Boolean primaryIdentifiersAlreadyExist = readResult.isSuccess() &&
                getPrimaryIdentifier(readResult.getResourceModel()).similar(getPrimaryIdentifier(model));
        if (primaryIdentifiersAlreadyExist) {
            this.logger.log(String.format("%s [%s] already exists",
                    ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString()));
            return ProgressEvent.defaultFailureHandler(new ResourceAlreadyExistsException(ResourceModel.TYPE_NAME, model.getFilterName()),
                    HandlerErrorCode.AlreadyExists);
        }

        proxy.injectCredentialsAndInvokeV2(Translator.translateToPutRequest(model),
                this.client::putMetricFilter);
        this.logger.log(String.format("%s [%s] created successfully",
            ResourceModel.TYPE_NAME, getPrimaryIdentifier(model).toString()));

        return ProgressEvent.defaultSuccessHandler(model);
    }
}
