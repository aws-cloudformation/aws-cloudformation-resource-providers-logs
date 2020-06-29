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

        return ProgressEvent.progress(model, callbackContext)
            .then(progress -> {
                if (!isUpdatable(model, previousModel)) {
                    return ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .errorCode(HandlerErrorCode.NotUpdatable)
                            .status(OperationStatus.FAILED)
                            .build();
                }
                return progress;
            })
            .then(progress ->
                preCreateCheck(proxy, callbackContext, proxyClient, model)
                    .done((response) -> {
                        if (response.metricFilters().isEmpty()) {
                            return ProgressEvent.defaultFailureHandler(new CfnNotFoundException(null), HandlerErrorCode.NotFound);
                        }
                        return ProgressEvent.progress(model, callbackContext);
                    })
            )
            .then(progress -> proxy.initiate("AWS-Logs-MetricFilter::Update", proxyClient, model, callbackContext)
                    .translateToServiceRequest(Translator::translateToUpdateRequest)
                    .makeServiceCall(this::updateResource)
                    .progress())
            .then(progress -> ProgressEvent.defaultSuccessHandler(model));
    }

    private boolean isUpdatable(final ResourceModel model, final ResourceModel previousModel) {
        // An update request MUST return a NotUpdatable error if the user attempts to change a property
        // that is defined as create-only in the resource provider schema.
        if (previousModel != null) {
            return previousModel.getFilterName().equals(model.getFilterName())
                    && previousModel.getLogGroupName().equals(model.getLogGroupName());

        }
        return true;
    }

    private PutMetricFilterResponse updateResource(
        final PutMetricFilterRequest awsRequest,
        final ProxyClient<CloudWatchLogsClient> proxyClient) {
        PutMetricFilterResponse awsResponse;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::putMetricFilter);
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

        logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }
}
