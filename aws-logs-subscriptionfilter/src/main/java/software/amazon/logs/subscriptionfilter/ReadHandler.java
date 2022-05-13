package software.amazon.logs.subscriptionfilter;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Objects;

public class ReadHandler extends BaseHandlerStd {
    private Logger logger;
    private static final String callGraphString = "AWS-Logs-SubscriptionFilter::Read";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();
        final String stackId = request.getStackId() == null ? "" : request.getStackId();

        logger.log(String.format("Invoking request for: %s for stack: %s", callGraphString, stackId));

        return proxy.initiate(callGraphString, proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((awsRequest, sdkProxyClient) -> readResource(awsRequest, sdkProxyClient , model, stackId))
                .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .status(OperationStatus.SUCCESS)
                        .resourceModel(Translator.translateFromReadResponse(awsResponse))
                        .build());
    }

    private DescribeSubscriptionFiltersResponse readResource(
            final DescribeSubscriptionFiltersRequest awsRequest,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final ResourceModel model,
            final String stackId) {
        DescribeSubscriptionFiltersResponse describeSubscriptionFiltersResponse = null;
        try {
            describeSubscriptionFiltersResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeSubscriptionFilters);
        } catch (Exception e) {
            handleException(e, logger, stackId);
        }

        if (describeSubscriptionFiltersResponse == null || describeSubscriptionFiltersResponse.subscriptionFilters().isEmpty()) {
            logger.log(String.format("Resource does not exist for request: %s", awsRequest.toString()));
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME,
                    Objects.toString(model.getPrimaryIdentifier()));
        }

        logger.log(String.format("Got response: %s" , describeSubscriptionFiltersResponse));
        return describeSubscriptionFiltersResponse;
    }

}
