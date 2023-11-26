package software.amazon.logs.loganomalydetector;

import software.amazon.awssdk.services.cloudwatchlogs.model.ListLogAnomalyDetectorsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ListLogAnomalyDetectorsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;


import java.util.ArrayList;
import java.util.List;

public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final ListLogAnomalyDetectorsResponse listLogAnomalyDetectorsResponse;

        try {
            listLogAnomalyDetectorsResponse = proxy.injectCredentialsAndInvokeV2(Translator.translateToListRequest(request.getNextToken()), ClientBuilder.getLogsClient()::listLogAnomalyDetectors);
        } catch (InvalidParameterException ex) {
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, ex);
        } catch (OperationAbortedException ex) {
            throw new CfnResourceConflictException(ex);
        } catch (ResourceNotFoundException ex) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
        } catch (final ServiceUnavailableException ex) {
            throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, ex);
        }

        String nextToken = listLogAnomalyDetectorsResponse.nextToken();

        final List<ResourceModel> models = Translator.translateFromListRequest(listLogAnomalyDetectorsResponse);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(nextToken)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
