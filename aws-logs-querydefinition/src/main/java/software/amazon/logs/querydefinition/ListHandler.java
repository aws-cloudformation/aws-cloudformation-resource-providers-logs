package software.amazon.logs.querydefinition;

import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeQueryDefinitionsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final List<ResourceModel> models = new ArrayList<>();

        DescribeQueryDefinitionsResponse describeQueryDefinitionsResponse;
        String nextToken = null;
        do {
            try {
                describeQueryDefinitionsResponse = proxy.injectCredentialsAndInvokeV2(Translator.translateToListRequest(nextToken),
                        ClientBuilder.getLogsClient()::describeQueryDefinitions);
                nextToken = describeQueryDefinitionsResponse.nextToken();
                models.addAll(describeQueryDefinitionsResponse.queryDefinitions().stream()
                        .map(qd -> ResourceModel.builder()
                                .queryDefinitionId(qd.queryDefinitionId())
                                .name(qd.name())
                                .queryString(qd.queryString())
                                .logGroupNames(qd.logGroupNames())
                                .build())
                        .collect(Collectors.toList()));
            } catch (InvalidParameterException ex) {
                throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, ex);
            } catch (final ServiceUnavailableException ex) {
                throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, ex);
            }
        } while (nextToken != null);


        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(models)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
