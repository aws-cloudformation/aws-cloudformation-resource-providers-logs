package software.amazon.logs.subscriptionfilter;

import static software.amazon.logs.common.MetricsConstants.CFN;
import static software.amazon.logs.common.MetricsConstants.SERVICE;
import static software.amazon.logs.subscriptionfilter.MetricsHelper.putSubscriptionFilterRequestMetrics;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersResponse;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;
import software.amazon.logs.common.MetricsConstants;
import software.amazon.logs.common.MetricsHelper;

public class ListHandler extends BaseHandlerStd {

    private static final String CALL_GRAPH = "AWS-Logs-SubscriptionFilter::List";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger,
        final MetricsLogger metrics
    ) {
        MetricsHelper.putProperty(metrics, MetricsConstants.OPERATION, CALL_GRAPH);

        final ResourceModel model = request.getDesiredResourceState();
        final String nextToken = request.getNextToken();

        return proxy
            .initiate(CALL_GRAPH, proxyClient, model, callbackContext)
            .translateToServiceRequest(cbModel -> Translator.translateToListRequest(cbModel, nextToken))
            .backoffDelay(getBackOffStrategy())
            .makeServiceCall((listRequest, _proxyClient) -> listResource(listRequest, proxyClient, logger, metrics))
            .retryErrorFilter((_request, exception, _proxyClient, _model, _context) -> isRetryableException(exception))
            .done(describeResponse ->
                ProgressEvent
                    .<ResourceModel, CallbackContext>builder()
                    .resourceModels(Translator.translateFromListResponse(describeResponse))
                    .nextToken(nextToken)
                    .status(OperationStatus.SUCCESS)
                    .build()
            );
    }

    /**
     * List Subscription Filters.
     *
     * @param awsRequest  The request object for listing the subscription filters.
     * @param proxyClient The proxy client used to execute API calls.
     * @param logger      The logger.
     * @param metrics     The metrics logger.
     * @return The response object containing the list of subscription filters.
     * @throws BaseHandlerException If an exception occurs while listing the resources.
     */
    DescribeSubscriptionFiltersResponse listResource(
        final DescribeSubscriptionFiltersRequest awsRequest,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger,
        final MetricsLogger metrics
    ) {
        DescribeSubscriptionFiltersResponse awsResponse;
        putSubscriptionFilterRequestMetrics(metrics, awsRequest);

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeSubscriptionFilters);
        } catch (final CloudWatchLogsException serviceException) {
            final BaseHandlerException handlerException = Translator.translateException(serviceException);

            logger.log(
                String.format(
                    "[LIST][EXCEPTION] Exception while listing subscription filters: %s %s",
                    serviceException.getClass().getSimpleName(),
                    serviceException.getMessage()
                )
            );

            MetricsHelper.putExceptionProperty(metrics, serviceException, SERVICE);
            MetricsHelper.putExceptionProperty(metrics, handlerException, CFN);

            throw handlerException;
        }

        MetricsHelper.putServiceMetrics(metrics, awsResponse);

        return awsResponse;
    }
}
