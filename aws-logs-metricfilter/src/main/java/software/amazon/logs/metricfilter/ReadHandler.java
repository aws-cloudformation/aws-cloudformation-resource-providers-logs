package software.amazon.logs.metricfilter;

import static software.amazon.logs.common.MetricsConstants.CFN;
import static software.amazon.logs.common.MetricsConstants.SERVICE;
import static software.amazon.logs.metricfilter.MetricsHelper.putMetricFilterRequestMetrics;

import java.util.Objects;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
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

    private static final String CALL_GRAPH = "AWS-Logs-MetricFilter::Read";

    @Override
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
            .makeServiceCall((awsRequest, client) -> readResource(awsRequest, client, model, logger, metrics))
            .handleError((serviceRequest, exception, client, resourceModel, context) -> handleError(exception, resourceModel, context))
            .done(awsResponse ->
                ProgressEvent
                    .<ResourceModel, CallbackContext>builder()
                    .status(OperationStatus.SUCCESS)
                    .resourceModel(Translator.translateFromReadResponse(awsResponse))
                    .build()
            );
    }

    private DescribeMetricFiltersResponse readResource(
        final DescribeMetricFiltersRequest awsRequest,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final ResourceModel model,
        final Logger logger,
        final MetricsLogger metrics
    ) {
        DescribeMetricFiltersResponse awsResponse;
        putMetricFilterRequestMetrics(metrics, awsRequest);

        final String filterNamePrefix = awsRequest.filterNamePrefix();
        final String logGroupName = awsRequest.logGroupName();

        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeMetricFilters);
        } catch (CloudWatchLogsException serviceException) {
            BaseHandlerException handlerException = Translator.translateException(serviceException);

            logger.log(
                String.format(
                    "[READ][EXCEPTION] Encountered exception reading metric filters with prefix %s in log group %s: %s: %s",
                    filterNamePrefix,
                    logGroupName,
                    serviceException.getClass().getSimpleName(),
                    serviceException.getMessage()
                )
            );

            MetricsHelper.putExceptionProperty(metrics, serviceException, SERVICE);
            MetricsHelper.putExceptionProperty(metrics, handlerException, CFN);

            throw handlerException;
        }

        MetricsHelper.putServiceMetrics(metrics, awsResponse);

        if (awsResponse != null && awsResponse.metricFilters().isEmpty()) {
            logger.log(
                String.format("[READ][FAILED] No metric filter with prefix %s exists in log group %s", filterNamePrefix, logGroupName)
            );
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, Objects.toString(model.getPrimaryIdentifier()));
        }

        logger.log(String.format("[READ][SUCCESS] Found metric filters with prefix %s in log group %s", filterNamePrefix, logGroupName));

        return awsResponse;
    }
}
