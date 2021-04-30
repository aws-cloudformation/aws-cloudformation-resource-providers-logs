package software.amazon.logs.resourcepolicy;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        if (model.getPolicyName() == null) {
            return ProgressEvent.defaultFailureHandler(new CfnInvalidRequestException(ResourceModel.TYPE_NAME, new NullPointerException()), HandlerErrorCode.InvalidRequest);
        }

        if (model.getPolicyDocument() == null) {
            return ProgressEvent.defaultFailureHandler(new CfnInvalidRequestException(ResourceModel.TYPE_NAME, new NullPointerException()), HandlerErrorCode.InvalidRequest);
        }

        if (!policyExists(proxyClient.client(), model)) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
        }

        try {
            proxy.injectCredentialsAndInvokeV2(Translator.translateToPutRequest(model), ClientBuilder.getLogsClient()::putResourcePolicy);
        } catch (final InvalidParameterException e) {
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
        } catch (final ServiceUnavailableException e) {
            throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s [%s] successfully updated.", ResourceModel.TYPE_NAME, model.getPolicyName()));


        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
