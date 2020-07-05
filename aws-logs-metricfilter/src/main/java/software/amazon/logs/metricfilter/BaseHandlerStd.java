package software.amazon.logs.metricfilter;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.CallChain;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
  @Override
  public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final Logger logger) {
    return handleRequest(
      proxy,
      request,
      callbackContext != null ? callbackContext : new CallbackContext(),
      proxy.newProxy(ClientBuilder::getClient),
      logger
    );
  }

  protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final ProxyClient<CloudWatchLogsClient> proxyClient,
    final Logger logger);

  protected CallChain.Completed<DescribeMetricFiltersRequest, DescribeMetricFiltersResponse, CloudWatchLogsClient, ResourceModel, CallbackContext>
    preCreateCheck(final AmazonWebServicesClientProxy proxy,
                   final CallbackContext callbackContext,
                   final ProxyClient<CloudWatchLogsClient> proxyClient,
                   final ResourceModel model) {

    return proxy.initiate("AWS-Logs-MetricFilter::PreExistenceCheck", proxyClient, model, callbackContext)
            .translateToServiceRequest(Translator::translateToReadRequest)
            .makeServiceCall((awsRequest, sdkProxyClient) -> sdkProxyClient.injectCredentialsAndInvokeV2(awsRequest, sdkProxyClient.client()::describeMetricFilters))
            .handleError((request, exception, client, model1, context1) -> {
                // Consider ResourceNotFound as successful for pre exist check
              if(exception instanceof ResourceNotFoundException) {
                    return ProgressEvent.progress(model, callbackContext);
                }
              else if (exception instanceof InvalidParameterException) {
                return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.InvalidRequest, exception.getMessage());
              }
              else if (exception instanceof ServiceUnavailableException) {
                return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.ServiceInternalError, exception.getMessage());
              }
              return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.InternalFailure, "Internal Error");
            });
  }
}
