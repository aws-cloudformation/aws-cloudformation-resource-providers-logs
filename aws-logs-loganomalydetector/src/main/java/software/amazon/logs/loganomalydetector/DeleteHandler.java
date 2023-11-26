package software.amazon.logs.loganomalydetector;


import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogAnomalyDetectorResponse;

public class DeleteHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-Logs-LogAnomalyDetector::Delete", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToDeleteRequest)
                                .makeServiceCall((awsRequest, client) -> {
                                    DeleteLogAnomalyDetectorResponse awsResponse = null;
                                    try {
                                        awsResponse = client.injectCredentialsAndInvokeV2(awsRequest, client.client()::deleteLogAnomalyDetector);
                                    } catch (InvalidParameterException ex) {
                                        throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, ex);
                                    } catch (ResourceNotFoundException ex) {
                                        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.anomalyDetectorArn());
                                    } catch (ServiceUnavailableException ex) {
                                        throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, ex);
                                    }

                                    logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
                                    return awsResponse;
                                })
                                .done(response -> ProgressEvent.defaultSuccessHandler(null))
                );
    }
}
