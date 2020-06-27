package software.amazon.logs.metricfilter;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
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

    /**
     * We need this check because, despite what the API docs say, create/update SDK APIs do NOT return "resource not found"
     */
  protected boolean exists(final ProxyClient<CloudWatchLogsClient> proxyClient,
                           final ResourceModel model) throws AwsServiceException {

      final DescribeMetricFiltersRequest translateToReadRequest = Translator.translateToReadRequest(model);
      final DescribeMetricFiltersResponse response;
      try {
          response = proxyClient.injectCredentialsAndInvokeV2(translateToReadRequest, proxyClient.client()::describeMetricFilters);
          return !response.metricFilters().isEmpty();
      } catch (final AwsServiceException e) {
          BaseHandlerException newException = Translator.translateException(e);
          if (newException instanceof CfnNotFoundException) {
              return false;
          }
          throw e;
      }
  }
}
