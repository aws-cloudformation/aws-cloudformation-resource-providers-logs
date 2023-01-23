package software.amazon.logs.metricfilter;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.RetryableException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.CallChain;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.delay.Exponential;

import java.time.Duration;

import static software.amazon.awssdk.core.internal.retry.SdkDefaultRetrySetting.RETRYABLE_STATUS_CODES;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
    final Duration minDelay = Duration.ofSeconds(10);
    final Duration timeout = Duration.ofMinutes(30);

    // Uluru level exponential back off strategy. Starting with min delay of 10 seconds, until 30 mins is reached.
    protected final Exponential backoffStrategy = Exponential.of()
            .minDelay(minDelay)
            .timeout(timeout)
            .build();

    CallChain.ExceptionPropagate<CloudWatchLogsRequest, Exception, CloudWatchLogsClient, ResourceModel, CallbackContext, ProgressEvent<ResourceModel, CallbackContext>> handleError = (
                    CloudWatchLogsRequest request,
                    Exception exception,
                    ProxyClient<CloudWatchLogsClient> client,
                    ResourceModel resourceModel,
                    CallbackContext context) -> {
        if (exception.getMessage().toLowerCase().contains("rate exceeded")) {
            throw RetryableException.builder().cause(exception).build();
        }
        if (exception instanceof AwsServiceException && RETRYABLE_STATUS_CODES.contains(((AwsServiceException) exception).statusCode())) {
            throw RetryableException.builder().cause(exception).build();
        }
        return ProgressEvent.failed(resourceModel, context, HandlerErrorCode.GeneralServiceException, exception.getMessage());
    };

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger
    ) {
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
            final Logger logger
    );

    protected boolean exists(final ProxyClient<CloudWatchLogsClient> proxyClient, final ResourceModel model) throws AwsServiceException {
        final DescribeMetricFiltersRequest translateToReadRequest = Translator.translateToReadRequest(model);
        final DescribeMetricFiltersResponse response;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(translateToReadRequest, proxyClient.client()::describeMetricFilters);
            if (response == null || response.metricFilters() == null) {
                return false;
            }
            if (!response.hasMetricFilters()) {
                return false;
            }
            if (response.metricFilters().isEmpty()) {
                return false;
            }
            return model.getFilterName().equals(response.metricFilters().get(0).filterName());
        } catch (final ResourceNotFoundException e) {
            return false;
        }
    }
}
