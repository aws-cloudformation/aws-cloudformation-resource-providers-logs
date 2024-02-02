package software.amazon.logs.destination;

import static software.amazon.logs.common.MetricsConstants.CFN;
import static software.amazon.logs.common.MetricsConstants.SERVICE;
import static software.amazon.logs.destination.MetricsHelper.putDestinationRequestMetrics;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteDestinationRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteDestinationResponse;
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

public class DeleteHandler extends BaseHandlerStd {

    public static final String CALL_GRAPH = "AWS-Logs-Destination::Delete";

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
            .translateToServiceRequest(Translator::translateToDeleteRequest)
            .backoffDelay(getBackOffStrategy())
            .makeServiceCall((awsRequest, client) -> deleteResource(awsRequest, client, logger, metrics))
            .retryErrorFilter((_request, exception, _proxyClient, _model, _context) -> isRetryableException(exception))
            .done(_deleteResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder().status(OperationStatus.SUCCESS).build());
    }

    /**
     * Deletes a Destination.
     *
     * @param awsRequest  The request for deleting the resource.
     * @param proxyClient The proxy client used to execute API calls.
     * @param logger      The logger.
     * @param metrics     The metrics logger.
     * @return The response from the AWS service after deleting the resource.
     * @throws BaseHandlerException If an exception occurs while deleting the resource.
     */
    private DeleteDestinationResponse deleteResource(
        final DeleteDestinationRequest awsRequest,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger,
        final MetricsLogger metrics
    ) {
        DeleteDestinationResponse awsResponse;
        putDestinationRequestMetrics(metrics, awsRequest);

        try {
            // Delete destination
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteDestination);
        } catch (CloudWatchLogsException serviceException) {
            final BaseHandlerException handlerException = Translator.translateException(serviceException);

            logger.log(
                String.format(
                    "[DELETE][EXCEPTION] Encountered exception while deleting destination %s: %s: %s",
                    awsRequest.destinationName(),
                    serviceException.getClass().getSimpleName(),
                    serviceException.getMessage()
                )
            );

            MetricsHelper.putExceptionProperty(metrics, serviceException, SERVICE);
            MetricsHelper.putExceptionProperty(metrics, handlerException, CFN);

            throw handlerException;
        }

        MetricsHelper.putServiceMetrics(metrics, awsResponse);
        logger.log(String.format("[DELETE][SUCCESS] Deleted destination %s", awsRequest.destinationName()));

        return awsResponse;
    }
}
