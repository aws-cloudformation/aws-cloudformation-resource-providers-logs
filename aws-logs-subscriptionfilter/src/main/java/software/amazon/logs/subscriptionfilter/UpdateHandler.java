package software.amazon.logs.subscriptionfilter;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.LimitExceededException;
import software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutSubscriptionFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutSubscriptionFilterResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();
        final ResourceModel previousModel = request.getPreviousResourceState();
        final String stackId = request.getStackId() == null ? "" : request.getStackId();

        this.logger.log(String.format("Got request to update model to %s from model %s", model, previousModel));

        return proxy.initiate("AWS-Logs-SubscriptionFilter::Update", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToUpdateRequest)
                .makeServiceCall((r, c) -> updateResource(model, r, c, stackId))
                .success();
    }

    private PutSubscriptionFilterResponse updateResource(
            final ResourceModel model,
            final PutSubscriptionFilterRequest awsRequest,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final String stackId) {
        PutSubscriptionFilterResponse awsResponse;
        try {
            boolean exists = doesResourceExist(proxyClient, model);
            if (!exists) {
                logger.log(String.format("Resource does not exist for request: %s in stack %s", awsRequest.toString(), stackId));
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
            }

            logger.log(String.format("Resource exists; attempting updating for request: %s in stack %s", awsRequest.toString(), stackId));
            return proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::putSubscriptionFilter);
        } catch (final ResourceNotFoundException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnNotFoundException(e);
        } catch (final InvalidParameterException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
        } catch (final LimitExceededException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnServiceLimitExceededException(e);
        } catch (final ServiceUnavailableException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnServiceInternalErrorException(e);
        } catch (final OperationAbortedException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnResourceConflictException(e);
        }
    }
}
