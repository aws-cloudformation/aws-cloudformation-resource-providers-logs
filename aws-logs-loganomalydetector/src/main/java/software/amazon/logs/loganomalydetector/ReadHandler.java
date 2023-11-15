package software.amazon.logs.loganomalydetector;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogAnomalyDetectorResponse;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;

public class ReadHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {

        return proxy.initiate("AWS-Logs-LogAnomalyDetector::Read", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((awsRequest, client) -> {
                    GetLogAnomalyDetectorResponse awsResponse;
                    try {
                        awsResponse = client.injectCredentialsAndInvokeV2(awsRequest, client.client()::getLogAnomalyDetector);
                    } catch (InvalidParameterException ex) {
                        throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, ex);
                    } catch (OperationAbortedException ex) {
                        throw new CfnResourceConflictException(ex);
                    } catch (ResourceNotFoundException ex) {
                        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.anomalyDetectorArn());
                    } catch (final ServiceUnavailableException ex) {
                        throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, ex);
                    }

                    logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
                    return awsResponse;
                })

                .done(awsResponse -> ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(awsResponse, request.getDesiredResourceState().getAnomalyDetectorArn())));
    }
}
