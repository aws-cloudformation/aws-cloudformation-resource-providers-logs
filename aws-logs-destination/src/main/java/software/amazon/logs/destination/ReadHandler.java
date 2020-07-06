package software.amazon.logs.destination;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {

    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {
        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        return proxy.initiate("AWS-Logs-Destination::Read", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((awsRequest, sdkProxyClient) -> readResource(awsRequest, sdkProxyClient, model))
                .done(this::constructResourceModelFromResponse);

    }

    private DescribeDestinationsResponse readResource(final DescribeDestinationsRequest awsRequest,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final ResourceModel model) {
        DescribeDestinationsResponse awsResponse = null;

        try {
            awsResponse =
                    proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeDestinations);
            logger.log(String.format("%s resource with name %s has been successfully read", ResourceModel.TYPE_NAME,
                    model.getDestinationName()));
        } catch (CloudWatchLogsException e) {
            logger.log(String.format("Exception while reading the %s with name %s. %s", ResourceModel.TYPE_NAME,
                    model.getDestinationName(), e));
            Translator.translateException(e);
        }
        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(DescribeDestinationsRequest describeDestinationsRequest,
            DescribeDestinationsResponse describeDestinationsResponse,
            ProxyClient<CloudWatchLogsClient> cloudWatchLogsClientProxyClient,
            ResourceModel resourceModel,
            CallbackContext callbackContext) {
        ResourceModel translatedResourceModel = Translator.translateFromReadResponse(describeDestinationsResponse);

        if (translatedResourceModel == null) {
            logger.log(String.format("%s with name %s Resource does not exist", ResourceModel.TYPE_NAME,
                    resourceModel.getDestinationName()));
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, resourceModel.getDestinationName());
        }

        return ProgressEvent.defaultSuccessHandler(translatedResourceModel);
    }

}
