package software.amazon.logs.subscriptionfilter;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteSubscriptionFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteSubscriptionFilterResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();
        final String callGraphString = "AWS-Logs-SubscriptionFilter::Delete";
        final String stackId = request.getStackId() == null ? "" : request.getStackId();

        logger.log(String.format("Invoking %s request for model: %s with StackID: %s", callGraphString, model, stackId));

        return proxy.initiate(callGraphString, proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToDeleteRequest)
                .makeServiceCall((_request, _callbackContext) -> deleteResource(_request, proxyClient, stackId))
                .done(awsResponse -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .status(OperationStatus.SUCCESS)
                        .build());
    }

    private DeleteSubscriptionFilterResponse deleteResource(
            final DeleteSubscriptionFilterRequest awsRequest,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final String stackId) {
        DeleteSubscriptionFilterResponse awsResponse;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteSubscriptionFilter);
        } catch (ResourceNotFoundException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnNotFoundException(e);
        } catch (InvalidParameterException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnInvalidRequestException(e);
        } catch (OperationAbortedException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnResourceConflictException(e);
        } catch (ServiceUnavailableException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnServiceInternalErrorException(e);
        }

        logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }
}
