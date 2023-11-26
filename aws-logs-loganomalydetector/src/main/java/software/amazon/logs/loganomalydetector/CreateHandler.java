package software.amazon.logs.loganomalydetector;

import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogAnomalyDetectorResponse;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.LimitExceededException;
import software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceAlreadyExistsException;


public class CreateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)

                .then(progress ->
                        proxy.initiate("AWS-Logs-LogAnomalyDetector::Create", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToCreateRequest)
                                .makeServiceCall((awsRequest, client) -> {
                                    CreateLogAnomalyDetectorResponse awsResponse = null;
                                    try {
                                        awsResponse = client.injectCredentialsAndInvokeV2(awsRequest, client.client()::createLogAnomalyDetector);
                                    } catch (InvalidParameterException ex) {
                                        throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, ex);
                                    } catch (LimitExceededException ex) {
                                        // LimitExceeded is the only exception thrown by createLogAnomalyImplementation
                                        // Acts as an AlreadyExistsException as there can only be 1 LAD per log group
                                        throw new CfnAlreadyExistsException(ex);
                                    } catch (OperationAbortedException ex) {
                                        throw new CfnResourceConflictException(ex);
                                    } catch (ServiceUnavailableException ex) {
                                        throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, ex);
                                    }
                                    logger.log(String.format("%s successfully created.", ResourceModel.TYPE_NAME));

                                    return awsResponse;
                                })
                                .done(response -> ProgressEvent.defaultSuccessHandler(
                                        ResourceModel.builder()
                                                .anomalyDetectorArn(response.anomalyDetectorArn())
                                                .build()
                                )));
    }
}
