package software.amazon.logs.logstream;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.*;

import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;

import java.util.Objects;

import com.amazonaws.util.StringUtils;

public class ReadHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();
        logger.log(String.format("LogStreamNamePrefix: %s", model.getLogStreamName()));
        final String stackId = request.getStackId() == null ? "" : request.getStackId();

        logger.log(String.format("Invoking request for: %s for stack: %s", "AWS-Logs-LogStream::Read", stackId));

        // if log group name is null then return an error message
        if (model == null || StringUtils.isNullOrEmpty(model.getLogGroupName())){
            return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.InvalidRequest, "Log Group Name cannot be empty");
        }

        return proxy.initiate("AWS-Logs-LogStream::Read", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((awsRequest, sdkProxyClient) -> readResource(awsRequest, sdkProxyClient , model, stackId))
                .handleError((cbRequest, exception, cbProxyClient, cbModel, cbContext) -> handleError(cbRequest, exception, cbProxyClient, cbModel, cbContext))
                .done((awsResponse) -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .status(OperationStatus.SUCCESS)
                            .resourceModel(Translator.translateFromReadResponse(awsResponse, model))
                            .build());
    }

    private DescribeLogStreamsResponse readResource(
            final DescribeLogStreamsRequest awsRequest,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final ResourceModel model,
            final String stackId) {
        DescribeLogStreamsResponse describeLogStreamsResponse = null;
            describeLogStreamsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeLogStreams);

        if (describeLogStreamsResponse == null || describeLogStreamsResponse.logStreams().isEmpty()
                || !describeLogStreamsResponse.logStreams().get(0).logStreamName().equals(model.getLogStreamName())) {
            logger.log(String.format("Resource does not exist for request: %s", awsRequest.toString()));
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, Objects.toString(model.getPrimaryIdentifier()));
        }

        logger.log(String.format("Got response: %s", describeLogStreamsResponse));
        return describeLogStreamsResponse;
    }
}
