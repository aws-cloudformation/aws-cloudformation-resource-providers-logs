package software.amazon.logs.loganomalydetector;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.UpdateLogAnomalyDetectorResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;


public class UpdateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-Logs-LogAnomalyDetector::Update", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToUpdateRequest)
                                .makeServiceCall((awsRequest, client) -> {
                                    UpdateLogAnomalyDetectorResponse awsResponse = null;
                                    try {
                                        awsResponse = client.injectCredentialsAndInvokeV2(awsRequest, client.client()::updateLogAnomalyDetector);
                                    } catch (final InvalidParameterException e) {
                                        throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
                                    } catch (final ServiceUnavailableException e) {
                                        throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, e);
                                    } catch (ResourceNotFoundException ex) {
                                        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.anomalyDetectorArn());
                                    } catch (OperationAbortedException ex) {
                                        throw new CfnResourceConflictException(ex);
                                    }

                                    logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
                                    return awsResponse;
                                })
                                .done(awsRequest -> ProgressEvent.defaultSuccessHandler(request.getDesiredResourceState())));
    }
}
