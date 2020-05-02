package software.amazon.logs.metricfilter;

import java.util.Objects;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

public class CreateHandler extends BaseHandlerStd {
    private Logger logger;
    private static final int MAX_LENGTH_METRIC_FILTER_NAME = 512;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        // resource can auto-generate a name if not supplied by caller.
        // this logic should move up into the CloudFormation engine, but
        // currently exists here for backwards-compatibility with existing models
        if (StringUtils.isNullOrEmpty(model.getFilterName())) {
            model.setFilterName(
                    IdentifierUtils.generateResourceIdentifier(
                            request.getLogicalResourceIdentifier(),
                            request.getClientRequestToken(),
                            MAX_LENGTH_METRIC_FILTER_NAME
                    )
            );
        }

        return ProgressEvent.progress(model, callbackContext)
            // target API does not support 'ResourceAlreadyExistsException' (see https://docs.aws.amazon.com/AmazonCloudWatchLogs/latest/APIReference/API_PutMetricFilter.html#API_PutMetricFilter_Errors)
            // so the following check is required
            .then(progress -> checkForPreCreateResourceExistence(proxy, proxyClient, request, progress))
            .then(progress ->
                proxy.initiate("AWS-Logs-MetricFilter::Create", proxyClient, model, callbackContext)
                    .translateToServiceRequest(Translator::translateToCreateRequest)
                    .makeServiceCall(this::createResource)
                    .progress())
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));

    }

    private ProgressEvent<ResourceModel, CallbackContext> checkForPreCreateResourceExistence(
        final AmazonWebServicesClientProxy proxy,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final ResourceHandlerRequest<ResourceModel> request,
        final ProgressEvent<ResourceModel,
        CallbackContext> progressEvent) {
        final ResourceModel model = progressEvent.getResourceModel();
        final CallbackContext callbackContext = progressEvent.getCallbackContext();
        try {
            new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger);
            logger.log(model.getPrimaryIdentifier() + " already exists!");
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, Objects.toString(model.getPrimaryIdentifier()));
        } catch (CfnNotFoundException e) {
            logger.log(model.getPrimaryIdentifier() + " does not exist; creating the resource.");
            return ProgressEvent.progress(model, callbackContext);
        }
    }

    /**
     * Implement client invocation of the create request through the proxyClient, which is already initialised with
     * caller credentials, correct region and retry settings
     * @param awsRequest the aws service request to create a resource
     * @param proxyClient the aws service client to make the call
     * @return awsResponse create resource response
     */
    private AwsResponse createResource(
        final PutMetricFilterRequest awsRequest,
        final ProxyClient<CloudWatchLogsClient> proxyClient) {
        AwsResponse awsResponse;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::putMetricFilter);
        } catch (final AwsServiceException e) {
            logger.log("Error trying to create resource: " + e.getMessage());
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s successfully created.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

}
