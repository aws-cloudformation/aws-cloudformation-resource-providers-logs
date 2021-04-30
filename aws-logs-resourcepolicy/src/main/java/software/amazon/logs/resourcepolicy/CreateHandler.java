package software.amazon.logs.resourcepolicy;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutResourcePolicyResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        if (policyExists(proxyClient.client(), model)) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
        }

        PutResourcePolicyResponse putResourcePolicyResponse = invokePutResourcePolicyCall(proxy, model);

        logger.log(String.format("%s [%s] successfully created.", ResourceModel.TYPE_NAME, model.getPolicyName()));

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    private PutResourcePolicyResponse invokePutResourcePolicyCall(AmazonWebServicesClientProxy proxy, ResourceModel model) {
        try {
            return proxy.injectCredentialsAndInvokeV2(Translator.translateToPutRequest(model), ClientBuilder.getLogsClient()::putResourcePolicy);
        } catch (InvalidParameterException ex) {
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, ex);
        } catch (ServiceUnavailableException ex) {
            throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, ex);
        }
    }
}
