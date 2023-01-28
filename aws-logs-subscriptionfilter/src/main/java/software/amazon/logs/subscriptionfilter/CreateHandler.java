package software.amazon.logs.subscriptionfilter;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.exceptions.*;
import software.amazon.cloudformation.proxy.*;
import software.amazon.cloudformation.resource.IdentifierUtils;

public class CreateHandler extends BaseHandlerStd {
    private Logger logger;
    static final int PHYSICAL_RESOURCE_ID_MAX_LENGTH = 512;
    private static final String DEFAULT_SUBSCRIPTION_FILTER_NAME_PREFIX = "SubscriptionFilter";
    private static final String CALL_GRAPH_STRING = "AWS-Logs-SubscriptionFilter::Create";
    private static final String ERROR_CODE_INVALID_PARAMETER_EXCEPTION = "InvalidParameterException";

    private final ReadHandler readHandler;

    public CreateHandler() {
        super();
        this.readHandler = new ReadHandler();
    }

    @VisibleForTesting
    public CreateHandler(CloudWatchLogsClient logsClient, ReadHandler readHandler) {
        super(logsClient);
        this.readHandler = new ReadHandler();
    }

    /**
     * Generate the FilterName for the model from the Request
     *
     * @param request - the incoming request
     * @return - the generated filter name
     */
    private String generateSubscriptionFilterName(final ResourceHandlerRequest<ResourceModel> request) {
        final String logicalIdentifier = StringUtils.defaultString(request.getLogicalResourceIdentifier(), DEFAULT_SUBSCRIPTION_FILTER_NAME_PREFIX);
        final String clientRequestToken = request.getClientRequestToken();

        if (request.getStackId() != null) {
            return IdentifierUtils.generateResourceIdentifier(request.getStackId(), logicalIdentifier,
                    clientRequestToken, PHYSICAL_RESOURCE_ID_MAX_LENGTH);
        } else {
            return IdentifierUtils.generateResourceIdentifier(logicalIdentifier, clientRequestToken, PHYSICAL_RESOURCE_ID_MAX_LENGTH);
        }
    }

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();
        final String stackId = request.getStackId();

        if (StringUtils.isBlank(model.getFilterName())) {
            final String resourceIdentifier = generateSubscriptionFilterName(request);
            model.setFilterName(resourceIdentifier);
            logger.log(String.format("Filter name not present. Generated: %s as FilterName for stackID: %s", resourceIdentifier, request.getStackId()));
        }

        return ProgressEvent.progress(model, callbackContext)
                .then(progress ->
                        preCreateCheck(proxy, callbackContext, proxyClient, model).done(response -> {
                            if (filterNameExists(response, model)) {
                                return ProgressEvent.defaultFailureHandler(
                                        new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString()),
                                        HandlerErrorCode.AlreadyExists
                                );
                            }
                            return ProgressEvent.progress(model, callbackContext);
                        }))
                        .then(progress ->
                        proxy.initiate(CALL_GRAPH_STRING, proxyClient, model, callbackContext)
                                .translateToServiceRequest(Translator::translateToCreateRequest)
                                .makeServiceCall((filterRequest, client) -> client
                                        .injectCredentialsAndInvokeV2(filterRequest,
                                                client.client()::putSubscriptionFilter))
                                .handleError((req, e, proxyClient1, model1, context) ->  {
                                    // invalid parameter exception needs to be retried
                                    if (e instanceof AwsServiceException && ((AwsServiceException)e).awsErrorDetails() != null) {
                                        final AwsServiceException awsServiceException = (AwsServiceException) e;
                                        if (awsServiceException.awsErrorDetails().errorCode().equals(ERROR_CODE_INVALID_PARAMETER_EXCEPTION)) {
                                            throw new CfnThrottlingException(e);
                                        }
                                    }

                                    final HandlerErrorCode handlerErrorCode = getExceptionDetails(e, logger, stackId);
                                    return ProgressEvent.defaultFailureHandler(e, handlerErrorCode);
                                }).progress()
                )
                .then(progress -> readHandler
                        .handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }
}
