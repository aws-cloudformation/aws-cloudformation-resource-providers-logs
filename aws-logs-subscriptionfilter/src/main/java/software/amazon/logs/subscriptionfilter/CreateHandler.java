package software.amazon.logs.subscriptionfilter;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;
import software.amazon.cloudformation.exceptions.*;
import software.amazon.cloudformation.proxy.*;
import software.amazon.cloudformation.resource.IdentifierUtils;

public class CreateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {
        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        if (StringUtils.isNullOrEmpty(model.getFilterName())) {
            final String resourceIdentifier = IdentifierUtils.generateResourceIdentifier(request.getLogicalResourceIdentifier(), request.getClientRequestToken());
            logger.log(String.format("Filter name not present. Generated: %s as FilterName", resourceIdentifier));
            model.setFilterName(resourceIdentifier);
        }

        return proxy.initiate("AWS-Logs-SubscriptionFilter::Create", proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToCreateRequest)
                .makeServiceCall((r, c) -> createResource(model, r, c))
                .success();
    }

    private PutSubscriptionFilterResponse createResource(
            final ResourceModel model,
            final PutSubscriptionFilterRequest awsRequest,
            final ProxyClient<CloudWatchLogsClient> proxyClient) {
        try {
            final boolean exists = doesResourceExist(proxyClient, model);
            if (exists) {
                logger.log(String.format("Resource exists; not creating resource for model: %s", model));
                throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
            }

            logger.log(String.format("Resource doesn't exist. Creating a new one for model %s", model));
            return proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::putSubscriptionFilter);
        } catch (final InvalidParameterException e) {
            logException(e);
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
        } catch (final LimitExceededException e) {
            logException(e);
            throw new CfnServiceLimitExceededException(e);
        } catch (final OperationAbortedException e) {
            logException(e);
            throw new CfnResourceConflictException(e);
        } catch (final ServiceUnavailableException e) {
            logException(e);
            throw new CfnServiceInternalErrorException(e);
        }
    }

    private void logException(Exception e) {
        logger.log(String.format("%s: %s Message: %s Cause: %s", e.toString(), e, e.getMessage(), e.getCause()));
    }

}