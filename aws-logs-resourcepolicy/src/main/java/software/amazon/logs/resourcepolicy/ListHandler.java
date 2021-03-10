package software.amazon.logs.resourcepolicy;

import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeQueryDefinitionsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeResourcePoliciesResponse;
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

        DescribeResourcePoliciesResponse describeResourcePoliciesResponse;
        String nextToken = null;
        do {
            try {
                describeResourcePoliciesResponse = proxy.injectCredentialsAndInvokeV2(Translator.translateToListRequest(nextToken),
                        ClientBuilder.getLogsClient()::describeResourcePolicies);
                nextToken = describeResourcePoliciesResponse.nextToken();
                models.addAll(describeResourcePoliciesResponse.resourcePolicies().stream()
                        .map(qd -> ResourceModel.builder()
                                .name(qd.policyName())
                                .policyDocument(qd.policyDocument())
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
