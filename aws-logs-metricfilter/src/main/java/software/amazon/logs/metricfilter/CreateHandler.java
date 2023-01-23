package software.amazon.logs.metricfilter;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterResponse;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

public class CreateHandler extends BaseHandlerStd {
    private Logger logger;
    // if you change the value in the line below, please also update the resource schema
    private static final int MAX_LENGTH_METRIC_FILTER_NAME = 512;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        if (StringUtils.isNullOrEmpty(model.getFilterName())) {
            model.setFilterName(IdentifierUtils.generateResourceIdentifier(request.getLogicalResourceIdentifier(), request.getClientRequestToken(), MAX_LENGTH_METRIC_FILTER_NAME));
        }

        return proxy.initiate("AWS-Logs-MetricFilter::Create", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToCreateRequest)
                .backoffDelay(backoffStrategy)
                .makeServiceCall((r, c) -> createResource(model, r, c))
                .handleError(handleError)
                .done((x)-> ProgressEvent.<ResourceModel, CallbackContext>builder().status(OperationStatus.SUCCESS).resourceModel(model).build());
    }

    private PutMetricFilterResponse createResource(
            final ResourceModel model,
            final PutMetricFilterRequest awsRequest,
            final ProxyClient<CloudWatchLogsClient> proxyClient) {
        PutMetricFilterResponse response = null;
        try {
            boolean exists = exists(proxyClient, model);
            if (exists) {
                throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
            }
            logger.log(String.format("Resource doesn't exist. Creating a new one %s", ResourceModel.TYPE_NAME));
            response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::putMetricFilter);
        } catch (final CloudWatchLogsException e) {
            Translator.translateException(e);
        }
        return response;
    }

}
