package software.amazon.logs.metricfilter;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.LimitExceededException;
import software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
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

        this.logger.log(String.format("Trying to update model %s", model.getPrimaryIdentifier()));

        return proxy.initiate("AWS-Logs-MetricFilter::Update", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToUpdateRequest)
                .makeServiceCall((r, c) -> updateResource(model, r, c))
                .handleError(handleRateExceededError)
                .success();
    }

    private PutMetricFilterResponse updateResource(
            final ResourceModel model,
            final PutMetricFilterRequest awsRequest,
            final ProxyClient<CloudWatchLogsClient> proxyClient) {
        PutMetricFilterResponse awsResponse;
        try {
            boolean exists = exists(proxyClient, model);
            if (!exists) {
                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
            }
            logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
            return proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::putMetricFilter);
        } catch (final ResourceNotFoundException e) {
            logger.log("Resource not found. " + e.getMessage());
            throw new CfnNotFoundException(e);
        } catch (final InvalidParameterException e) {
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
        } catch (final LimitExceededException e) {
            throw new CfnServiceLimitExceededException(e);
        } catch (final ServiceUnavailableException e) {
            throw new CfnServiceInternalErrorException(e);
        } catch (final OperationAbortedException e) {
            throw new CfnResourceConflictException(e);
        }
    }
}
