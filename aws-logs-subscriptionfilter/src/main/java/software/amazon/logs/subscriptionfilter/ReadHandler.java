package software.amazon.logs.subscriptionfilter;

import static software.amazon.logs.subscriptionfilter.MetricsHelper.putSubscriptionFilterRequestMetrics;

import java.util.Objects;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersResponse;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;
import software.amazon.logs.common.MetricsConstants;
import software.amazon.logs.common.MetricsHelper;

public class ReadHandler extends BaseHandlerStd {

    private static final String CALL_GRAPH = "AWS-Logs-SubscriptionFilter::Read";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger,
        final MetricsLogger metrics
    ) {
        final ResourceModel model = request.getDesiredResourceState();

        return proxy
            .initiate(CALL_GRAPH, proxyClient, model, callbackContext)
            .translateToServiceRequest(Translator::translateToReadRequest)
            .backoffDelay(getBackOffStrategy())
            .makeServiceCall((cloudWatchLogsRequest, sdkProxyClient) ->
                readResource(cloudWatchLogsRequest, sdkProxyClient, model, logger, metrics)
            )
            .retryErrorFilter((_request, exception, _proxyClient, _model, _context) -> isRetryableException(exception))
            .done(awsResponse ->
                ProgressEvent
                    .<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.SUCCESS)
                    .resourceModel(Translator.translateFromReadResponse(awsResponse))
                    .build()
            );
    }

    /**
     * Read a Subscription Filter.
     *
     * @param awsRequest  The request for describing the subscription filters.
     * @param proxyClient The proxy client used to execute API calls.
     * @param model       The resource model representing the subscription filters.
     * @param logger      The logger.
     * @param metrics     The metrics logger.
     * @return The response containing the describe subscription filters information.
     * @throws BaseHandlerException If an exception occurs while reading the resource.
     */
    private DescribeSubscriptionFiltersResponse readResource(
        final DescribeSubscriptionFiltersRequest awsRequest,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final ResourceModel model,
        final Logger logger,
        final MetricsLogger metrics
    ) {
        DescribeSubscriptionFiltersResponse awsResponse;
        putSubscriptionFilterRequestMetrics(metrics, awsRequest);

        final String filterNamePrefix = awsRequest.filterNamePrefix();
        final String logGroupName = model.getLogGroupName();

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeSubscriptionFilters);
        } catch (CloudWatchLogsException serviceException) {
            final BaseHandlerException handlerException = Translator.translateException(serviceException);

            logger.log(
                String.format(
                    "[READ][EXCEPTION] Encountered exception with subscription filter prefix %s in log group %s: %s: %s",
                    filterNamePrefix,
                    logGroupName,
                    serviceException.getClass().getSimpleName(),
                    serviceException.getMessage()
                )
            );

            MetricsHelper.putExceptionProperty(metrics, serviceException, MetricsConstants.SERVICE);
            MetricsHelper.putExceptionProperty(metrics, handlerException, MetricsConstants.CFN);

            throw handlerException;
        }

        if (awsResponse != null && awsResponse.subscriptionFilters().isEmpty()) {
            logger.log(
                String.format(
                    "[READ][FAILED] No subscription filters found with prefix %s exists in log group %s",
                    filterNamePrefix,
                    logGroupName
                )
            );
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, Objects.toString(model.getPrimaryIdentifier()));
        }

        MetricsHelper.putServiceMetrics(metrics, awsResponse);
        logger.log(
            String.format("[READ][SUCCESS] Found subscription filters with prefix %s in log group %s", filterNamePrefix, logGroupName)
        );

        return awsResponse;
    }
}
