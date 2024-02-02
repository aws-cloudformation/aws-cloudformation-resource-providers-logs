package software.amazon.logs.metricfilter;

import static software.amazon.logs.common.MetricsConstants.CFN;
import static software.amazon.logs.common.MetricsConstants.SERVICE;
import static software.amazon.logs.metricfilter.MetricsHelper.putMetricFilterRequestMetrics;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;
import software.amazon.logs.common.MetricsConstants;
import software.amazon.logs.common.MetricsHelper;

public class UpdateHandler extends BaseHandlerStd {

    private static final String CALL_GRAPH = "AWS-Logs-MetricFilter::Update";

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
            .translateToServiceRequest(Translator::translateToUpdateRequest)
            .backoffDelay(getBackOffStrategy())
            .makeServiceCall((serviceRequest, client) -> updateResource(model, serviceRequest, client, logger, metrics))
            .handleError((serviceRequest, exception, client, resourceModel, context) -> handleError(exception, resourceModel, context))
            .success();
    }

    private PutMetricFilterResponse updateResource(
        final ResourceModel model,
        final PutMetricFilterRequest awsRequest,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger,
        final MetricsLogger metrics
    ) {
        PutMetricFilterResponse awsResponse;
        putMetricFilterRequestMetrics(metrics, awsRequest);

        final String filterName = awsRequest.filterName();
        final String logGroupName = awsRequest.logGroupName();

        try {
            boolean exists = exists(proxyClient, model, Action.UPDATE, logger, metrics);
            if (!exists) {
                logger.log(String.format("[UPDATE][FAILED] Metric filter %s does not exist in log group %s", filterName, logGroupName));
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
            }
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::putMetricFilter);
        } catch (final CloudWatchLogsException serviceException) {
            BaseHandlerException handlerException = Translator.translateException(serviceException);

            logger.log(
                String.format(
                    "[UPDATE][EXCEPTION] Encountered exception with metric filter %s in log group %s: %s: %s",
                    filterName,
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
        logger.log(String.format("[UPDATE][SUCCESS] Updated metric filter %s in log group %s", filterName, logGroupName));

        return awsResponse;
    }
}
