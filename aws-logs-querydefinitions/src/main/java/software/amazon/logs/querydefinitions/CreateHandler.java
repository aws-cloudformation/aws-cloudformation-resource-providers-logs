package software.amazon.logs.querydefinitions;

import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutQueryDefinitionResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        if (model.getQueryDefinitionId() != null) {
            return ProgressEvent.defaultFailureHandler(new CfnInvalidRequestException("There should be no querydefinition ID in create requests"), HandlerErrorCode.InvalidRequest);
        }

        PutQueryDefinitionResponse putQueryDefinitionResponse = invokePutQueryDefinitionCall(proxy, model);
        model.setQueryDefinitionId(putQueryDefinitionResponse.queryDefinitionId());

        logger.log(String.format("%s [%s] successfully created.", ResourceModel.TYPE_NAME, model.getName()));

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }

    private PutQueryDefinitionResponse invokePutQueryDefinitionCall(AmazonWebServicesClientProxy proxy, ResourceModel model) {
        try {
            return proxy.injectCredentialsAndInvokeV2(Translator.translateToCreateRequest(model), ClientBuilder.getLogsClient()::putQueryDefinition);
        } catch (InvalidParameterException ex) {
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, ex);
        } catch (ResourceNotFoundException ex) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getName());
        } catch (ServiceUnavailableException ex) {
            throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, ex);
        }
    }


}
