package software.amazon.logs.destination;

import static software.amazon.logs.destination.MetricsHelper.putDestinationRequestMetrics;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
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

    private static final String CALL_GRAPH = "AWS-Logs-Destination::Read";

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

        return proxy
            .initiate(CALL_GRAPH, proxyClient, model, callbackContext)
            .translateToServiceRequest(Translator::translateToReadRequest)
            .backoffDelay(getBackOffStrategy())
            .makeServiceCall((awsRequest, sdkProxyClient) -> readResource(awsRequest, sdkProxyClient, model, logger, metrics))
            .retryErrorFilter((_request, exception, _proxyClient, _model, _context) -> isRetryableException(exception))
            .done(describeResponse ->
                ProgressEvent
                    .<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.SUCCESS)
                    .resourceModel(Translator.translateFromReadResponse(describeResponse))
                    .build()
            );
    }

    /**
     * Read a Destination.
     *
     * @param awsRequest  The request for describing the destination.
     * @param proxyClient The proxy client used to execute API calls.
     * @param model       The resource model representing the destination.
     * @param logger      The logger.
     * @param metrics     The metrics logger.
     * @return The response containing the describe destinations information.
     * @throws CfnNotFoundException If no destination with the specified prefix exists.
     * @throws BaseHandlerException If an exception occurs while reading the resource.
     */
    private DescribeDestinationsResponse readResource(
        final DescribeDestinationsRequest awsRequest,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final ResourceModel model,
        final Logger logger,
        final MetricsLogger metrics
    ) {
        DescribeDestinationsResponse awsResponse;
        putDestinationRequestMetrics(metrics, awsRequest);

        try {
            // Read destination
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeDestinations);
        } catch (CloudWatchLogsException serviceException) {
            final BaseHandlerException handlerException = Translator.translateException(serviceException);

            logger.log(
                String.format(
                    "[READ][EXCEPTION] Encountered exception while reading destination with name prefix %s: %s: %s",
                    awsRequest.destinationNamePrefix(),
                    serviceException.getClass().getSimpleName(),
                    serviceException.getMessage()
                )
            );

            MetricsHelper.putExceptionProperty(metrics, serviceException, MetricsConstants.SERVICE);
            MetricsHelper.putExceptionProperty(metrics, handlerException, MetricsConstants.CFN);

            throw handlerException;
        }

        if (awsResponse != null && awsResponse.destinations().isEmpty()) {
            logger.log(String.format("[READ][FAILED] No destination with prefix %s exists", awsRequest.destinationNamePrefix()));
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getDestinationName());
        }

        logger.log(
            String.format("[READ][SUCCESS] Destination resource with name %s has been successfully read", model.getDestinationName())
        );
        MetricsHelper.putServiceMetrics(metrics, awsResponse);

        return awsResponse;
    }
}
