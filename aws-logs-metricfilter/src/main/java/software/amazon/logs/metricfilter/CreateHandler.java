package software.amazon.logs.metricfilter;

import static software.amazon.logs.common.MetricsConstants.CFN;
import static software.amazon.logs.common.MetricsConstants.SERVICE;
import static software.amazon.logs.metricfilter.MetricsHelper.putMetricFilterRequestMetrics;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterResponse;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;
import software.amazon.logs.common.MetricsConstants;
import software.amazon.logs.common.MetricsHelper;

public class CreateHandler extends BaseHandlerStd {

    private static final String CALL_GRAPH = "AWS-Logs-MetricFilter::Create";
    // if you change the value in the line below, please also update the resource schema
    private static final int MAX_LENGTH_METRIC_FILTER_NAME = 512;

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

        if (StringUtils.isNullOrEmpty(model.getFilterName())) {
            model.setFilterName(
                IdentifierUtils.generateResourceIdentifier(
                    request.getLogicalResourceIdentifier(),
                    request.getClientRequestToken(),
                    MAX_LENGTH_METRIC_FILTER_NAME
                )
            );
        }

        return ProgressEvent
            .progress(model, callbackContext)
            .then(progress ->
                callbackContext.isPreCreateCheckDone() ? progress : preCreateCheck(proxyClient, model, callbackContext, logger, metrics)
            )
            .then(progress ->
                proxy
                    .initiate(CALL_GRAPH, proxyClient, model, callbackContext)
                    .translateToServiceRequest(Translator::translateToCreateRequest)
                    .backoffDelay(getBackOffStrategy())
                    .makeServiceCall((awsRequest, client) -> createResource(awsRequest, client, logger, metrics))
                    .handleError((serviceRequest, exception, client, resourceModel, context) ->
                        handleError(exception, resourceModel, context)
                    )
                    .done(x ->
                        ProgressEvent.<ResourceModel, CallbackContext>builder().status(OperationStatus.SUCCESS).resourceModel(model).build()
                    )
            )
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger, metrics));
    }

    /**
     * Performs pre-create checks before creating a resource.
     *
     * @param proxyClient     The proxy client used to execute API calls.
     * @param model           The resource model object.
     * @param callbackContext The callback context object.
     * @param logger          The logger.
     * @param metrics         The metrics logger.
     * @return The progress event indicating the status of the pre-create check.
     * @throws BaseHandlerException If an exception occurs while performing the pre-create check.
     */
    private ProgressEvent<ResourceModel, CallbackContext> preCreateCheck(
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final ResourceModel model,
        final CallbackContext callbackContext,
        final Logger logger,
        final MetricsLogger metrics
    ) {
        callbackContext.setPreCreateCheckDone(true);

        if (exists(proxyClient, model, Action.CREATE, logger, metrics)) {
            logger.log(
                String.format(
                    "[PRE_CREATE][FAILED] Metric Filter %s in log group %s already exists.",
                    model.getFilterName(),
                    model.getLogGroupName()
                )
            );

            return ProgressEvent.failed(
                model,
                callbackContext,
                HandlerErrorCode.AlreadyExists,
                String.format("Metric Filter with name %s already exists in log group %s.", model.getFilterName(), model.getLogGroupName())
            );
        }

        logger.log(
            String.format(
                "[PRE_CREATE][SUCCESS] Metric Filter %s in log group %s does not exist.",
                model.getFilterName(),
                model.getLogGroupName()
            )
        );

        return ProgressEvent.defaultInProgressHandler(callbackContext, CALLBACK_DELAY_SECONDS, model);
    }

    /**
     * Creates a Metric Filter.
     *
     * @param awsRequest  The request object used to create the destination.
     * @param proxyClient The proxy client used to execute API calls.
     * @param logger      The logger.
     * @param metrics     The metrics logger.
     * @return The response object containing the created destination.
     * @throws BaseHandlerException If an exception occurs while creating the resource.
     */
    private PutMetricFilterResponse createResource(
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
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::putMetricFilter);
        } catch (final CloudWatchLogsException serviceException) {
            final BaseHandlerException handlerException = Translator.translateException(serviceException);

            logger.log(
                String.format(
                    "[CREATE][EXCEPTION] Encountered exception with metric filter %s in log group %s: %s: %s",
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
        logger.log(String.format("[CREATE][SUCCESS] Created new metric filter %s in log group %s", filterName, logGroupName));

        return awsResponse;
    }
}
