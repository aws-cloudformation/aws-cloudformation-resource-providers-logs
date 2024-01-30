package software.amazon.logs.subscriptionfilter;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.Action;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
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

    private static final String CALL_GRAPH = "AWS-Logs-SubscriptionFilter::Create";
    private static final int PHYSICAL_RESOURCE_ID_MAX_LENGTH = 512;
    private static final String DEFAULT_SUBSCRIPTION_FILTER_NAME_PREFIX = "SubscriptionFilter";

    private ReadHandler getReadHandler() {
        return new ReadHandler();
    }

    public CreateHandler() {
        super();
    }

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

        if (StringUtils.isBlank(model.getFilterName())) {
            final String resourceIdentifier = generateSubscriptionFilterName(request);
            model.setFilterName(resourceIdentifier);
            logger.log(
                String.format(
                    "Filter name not present. Generated: %s as FilterName for stackID: %s",
                    resourceIdentifier,
                    request.getStackId()
                )
            );
        }

        return ProgressEvent
            .progress(model, callbackContext)
            .then(progress ->
                proxy
                    .initiate(CALL_GRAPH, proxyClient, model, callbackContext)
                    .translateToServiceRequest(Translator::translateToCreateRequest)
                    .backoffDelay(getBackOffStrategy())
                    .makeServiceCall((filterRequest, client) -> putResource(model, filterRequest, client, Action.CREATE, logger, metrics))
                    .retryErrorFilter((_request, exception, _proxyClient, _model, _context) -> isRetryableException(exception))
                    .done(_createResponse ->
                        ProgressEvent.<ResourceModel, CallbackContext>builder().status(OperationStatus.SUCCESS).resourceModel(model).build()
                    )
            )
            .then(progress -> getReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger, metrics));
    }

    /**
     * Generate the FilterName for the model from the request
     *
     * @param request The request
     * @return The generated filter name
     */
    private String generateSubscriptionFilterName(final ResourceHandlerRequest<ResourceModel> request) {
        final String logicalIdentifier = StringUtils.defaultString(
            request.getLogicalResourceIdentifier(),
            DEFAULT_SUBSCRIPTION_FILTER_NAME_PREFIX
        );
        final String clientRequestToken = request.getClientRequestToken();

        if (request.getStackId() != null) {
            return IdentifierUtils.generateResourceIdentifier(
                request.getStackId(),
                logicalIdentifier,
                clientRequestToken,
                PHYSICAL_RESOURCE_ID_MAX_LENGTH
            );
        } else {
            return IdentifierUtils.generateResourceIdentifier(logicalIdentifier, clientRequestToken, PHYSICAL_RESOURCE_ID_MAX_LENGTH);
        }
    }
}
