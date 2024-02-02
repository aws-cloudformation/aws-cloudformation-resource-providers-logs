package software.amazon.logs.destination;

import static software.amazon.logs.destination.MetricsHelper.putDestinationRequestMetrics;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;
import software.amazon.logs.common.MetricsHelper;
import software.amazon.logs.common.MetricsConstants;

public class ListHandler extends BaseHandlerStd {

    public static final String CALL_GRAPH = "AWS-Logs-Destination::List";

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger,
        final MetricsLogger metrics
    ) {
        MetricsHelper.putProperty(metrics, MetricsConstants.OPERATION, CALL_GRAPH);

        return proxy
            .initiate(CALL_GRAPH, proxyClient, request.getDesiredResourceState(), callbackContext)
            .translateToServiceRequest(Translator::translateToListRequest)
            .backoffDelay(getBackOffStrategy())
            .makeServiceCall((describeDestinationsRequest, client) -> listResource(describeDestinationsRequest, client, logger, metrics))
            .retryErrorFilter((_request, exception, _proxyClient, _model, _context) -> isRetryableException(exception))
            .done(describeResponse ->
                ProgressEvent
                    .<ResourceModel, CallbackContext>builder()
                    .resourceModels(Translator.translateFromListResponse(describeResponse))
                    .status(OperationStatus.SUCCESS)
                    .nextToken(describeResponse.nextToken())
                    .build()
            );
    }

    /**
     * List Destinations.
     *
     * @param awsRequest  The request object for listing the destinations.
     * @param proxyClient The proxy client used to execute API calls.
     * @param logger      The logger.
     * @param metrics     The metrics logger.
     * @return The response object containing the list of destinations.
     * @throws BaseHandlerException If an exception occurs while listing the resources.
     */
    private DescribeDestinationsResponse listResource(
        final DescribeDestinationsRequest awsRequest,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger,
        final MetricsLogger metrics
    ) {
        DescribeDestinationsResponse awsResponse;
        putDestinationRequestMetrics(metrics, awsRequest);

        try {
            // List destinations
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeDestinations);
            MetricsHelper.putServiceMetrics(metrics, awsResponse);
        } catch (CloudWatchLogsException serviceException) {
            final BaseHandlerException handlerException = Translator.translateException(serviceException);

            logger.log(
                String.format(
                    "[LIST][EXCEPTION] Exception while listing destinations: %s %s. %s",
                    serviceException.getClass().getSimpleName(),
                    serviceException.getMessage()
                )
            );

            MetricsHelper.putExceptionProperty(metrics, serviceException, MetricsConstants.SERVICE);
            MetricsHelper.putExceptionProperty(metrics, handlerException, MetricsConstants.CFN);

            throw handlerException;
        }

        return awsResponse;
    }
}
