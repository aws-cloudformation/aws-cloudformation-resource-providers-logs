package software.amazon.logs.destination;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationPolicyResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.CallChain;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request, final CallbackContext callbackContext,
            final Logger logger) {

        return handleRequest(proxy, request, callbackContext != null ? callbackContext : new CallbackContext(),
                proxy.newProxy(ClientBuilder::getClient), logger);
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy, final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext, final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger);

    protected CallChain.Completed<DescribeDestinationsRequest, DescribeDestinationsResponse, CloudWatchLogsClient, ResourceModel, CallbackContext> preCreateCheck(
            final AmazonWebServicesClientProxy proxy, final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient, final ResourceModel model) {

        return proxy.initiate("AWS-Logs-Destination::Create::PreExistenceCheck", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((awsRequest, sdkProxyClient) -> sdkProxyClient.injectCredentialsAndInvokeV2(awsRequest,
                        sdkProxyClient.client()::describeDestinations))
                .handleError((request, exception, client, model1, context1) -> {
                    ProgressEvent<ResourceModel, CallbackContext> progress;
                    if (exception instanceof InvalidParameterException) {
                        progress = ProgressEvent.failed(model, callbackContext, HandlerErrorCode.InvalidRequest,
                                exception.getMessage());
                    } else if (exception instanceof ServiceUnavailableException) {
                        progress = ProgressEvent.failed(model, callbackContext, HandlerErrorCode.ServiceInternalError,
                                exception.getMessage());
                    } else {
                        progress = ProgressEvent.progress(model, callbackContext);
                    }
                    return progress;
                });
    }

    protected boolean isDestinationListNullOrEmpty(final DescribeDestinationsResponse response) {

        return ! response.hasDestinations() || response.destinations().isEmpty();
    }

    protected ProgressEvent<ResourceModel, CallbackContext> putDestination(final AmazonWebServicesClientProxy proxy,
            final CallbackContext callbackContext, final ProxyClient<CloudWatchLogsClient> proxyClient,
            final ResourceModel model, final String callGraph, final Logger logger) {

        return proxy.initiate(callGraph, proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToCreateRequest)
                .makeServiceCall((awsRequest, sdkProxyClient) -> {
                    PutDestinationResponse putDestinationResponse = null;
                    try {
                        putDestinationResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest,
                                proxyClient.client()::putDestination);
                        logger.log(String.format("%s resource with name %s has been successfully created",
                                ResourceModel.TYPE_NAME, model.getDestinationName()));
                    } catch (AwsServiceException e) {
                        logger.log(String.format(
                                "Exception while invoking the putDestination API for the destination ID %s. %s ",
                                model.getDestinationName(), e));
                        Translator.translateException(e);
                    }
                    return putDestinationResponse;
                })
                .progress();
    }

    protected ProgressEvent<ResourceModel, CallbackContext> putDestinationPolicy(
            final AmazonWebServicesClientProxy proxy, final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient, final ResourceModel model, final String callGraph,
            final Logger logger) {

        return proxy.initiate(callGraph, proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToPutDestinationPolicyRequest)
                .makeServiceCall((awsRequest, sdkProxyClient) -> {
                    PutDestinationPolicyResponse response = null;
                    try {
                        response = sdkProxyClient.injectCredentialsAndInvokeV2(awsRequest,
                                sdkProxyClient.client()::putDestinationPolicy);
                        logger.log(String.format(
                                "Destination policy successfully updated for the resource with name %s has been " +
                                        "successfully created", model.getDestinationName()));
                    } catch (AwsServiceException e) {
                        logger.log(String.format(
                                "Exception while invoking the putDestinationPolicy API for the destination ID %s. %s ",
                                model.getDestinationName(), e));
                        Translator.translateException(e);
                    }
                    return response;
                })
                .progress();
    }

}
