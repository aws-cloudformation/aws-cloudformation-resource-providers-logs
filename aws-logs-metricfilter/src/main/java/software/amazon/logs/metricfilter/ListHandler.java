package software.amazon.logs.metricfilter;

import static software.amazon.logs.common.MetricsConstants.CFN;
import static software.amazon.logs.common.MetricsConstants.SERVICE;
import static software.amazon.logs.metricfilter.MetricsHelper.putMetricFilterRequestMetrics;

import java.util.List;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
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

    private static final String CALL_GRAPH = "AWS-Logs-MetricFilter::List";

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

        final DescribeMetricFiltersRequest awsRequest = Translator.translateToListRequest(request.getNextToken());
        putMetricFilterRequestMetrics(metrics, awsRequest);
        final String logGroupName = awsRequest.logGroupName();

        DescribeMetricFiltersResponse awsResponse;

        try {
            awsResponse = proxy.injectCredentialsAndInvokeV2(awsRequest, ClientBuilder.getClient()::describeMetricFilters);
        } catch (final CloudWatchLogsException serviceException) {
            BaseHandlerException handlerException = Translator.translateException(serviceException);

            logger.log(
                String.format(
                    "[LIST][EXCEPTION] Encountered exception listing metric filters in log group %s: %s: %s",
                    logGroupName,
                    serviceException.getClass().getSimpleName(),
                    serviceException.getMessage()
                )
            );
            MetricsHelper.putExceptionProperty(metrics, serviceException, SERVICE);
            MetricsHelper.putExceptionProperty(metrics, handlerException, CFN);

            throw handlerException;
        }

        final List<ResourceModel> models = Translator.translateFromListResponse(awsResponse);

        MetricsHelper.putServiceMetrics(metrics, awsResponse);
        logger.log(String.format("[LIST][SUCCESS] Listed metric filters in log group %s", logGroupName));

        return ProgressEvent
            .<ResourceModel, CallbackContext>builder()
            .resourceModels(models)
            .nextToken(awsResponse.nextToken())
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
