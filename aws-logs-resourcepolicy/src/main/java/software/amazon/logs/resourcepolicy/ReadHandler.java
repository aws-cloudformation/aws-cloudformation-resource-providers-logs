package software.amazon.logs.resourcepolicy;

import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeResourcePoliciesResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourcePolicy;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Optional;

public class ReadHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        DescribeResourcePoliciesResponse describeResourcePoliciesResponse;
        String nextToken = null;
        do {
            try {
                describeResourcePoliciesResponse = proxy.injectCredentialsAndInvokeV2(Translator.translateToListRequest(nextToken),
                        ClientBuilder.getLogsClient()::describeResourcePolicies);
                nextToken = describeResourcePoliciesResponse.nextToken();
                Optional<ResourcePolicy> resourcePolicy = describeResourcePoliciesResponse.resourcePolicies().stream()
                        .filter(rp -> rp.policyName().equals(model.getPolicyName()))
                        .findAny();
                if (resourcePolicy.isPresent()) {
                    return ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .resourceModel(model)
                            .status(OperationStatus.SUCCESS)
                            .build();
                }
            } catch (InvalidParameterException ex) {
                throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, ex);
            } catch (ServiceUnavailableException ex) {
                throw new CfnServiceInternalErrorException(ResourceModel.TYPE_NAME, ex);
            }
        } while (nextToken != null);

        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
    }
}
