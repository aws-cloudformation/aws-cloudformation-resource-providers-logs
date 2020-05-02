package software.amazon.logs.metricfilter;

import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();
        final ResourceModel previousModel = request.getPreviousResourceState();

        final boolean updatable = checkUpdatable(model, previousModel);
        if (!updatable) {
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .errorCode(HandlerErrorCode.NotUpdatable)
                    .status(OperationStatus.FAILED)
                    .build();
        }

        return ProgressEvent.progress(model, callbackContext)
            .then(progress ->
                proxy.initiate("AWS-Foo-Bar::Update", proxyClient, model, callbackContext)
                    .translateToServiceRequest(Translator::translateToUpdateRequest)
                    .makeServiceCall(this::updateResource)
                    .progress())

            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private boolean checkUpdatable(final ResourceModel model, final ResourceModel previousModel) {
        // An update request MUST return a NotUpdatable error if the user attempts to change a property
        // that is defined as create-only in the resource provider schema.
        if (previousModel != null) {
            return previousModel.getFilterName().equals(model.getFilterName())
                    && previousModel.getLogGroupName().equals(model.getLogGroupName());

        }
        return true;
    }

    private AwsResponse updateResource(
        final PutMetricFilterRequest awsRequest,
        final ProxyClient<CloudWatchLogsClient> proxyClient) {
        PutMetricFilterResponse awsResponse;
        try {

            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::putMetricFilter);
        } catch (final ResourceNotFoundException e) {
            logger.log("Resource not found. " + e.getMessage());
            throw new CfnNotFoundException(e);
        } catch (final AwsServiceException e) {
            logger.log("Error trying to update resource: " + e.getMessage());
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

}
