package software.amazon.logs.subscriptionfilter;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;
import software.amazon.cloudformation.exceptions.*;
import software.amazon.cloudformation.proxy.*;
import software.amazon.cloudformation.resource.IdentifierUtils;

public class CreateHandler extends BaseHandlerStd {
    private Logger logger;
    static final int PHYSICAL_RESOURCE_ID_MAX_LENGTH = 512;
    private static final String DEFAULT_SUBSCRIPTION_FILTER_NAME_PREFIX = "SubscriptionFilter";
    private static final String callGraphString = "AWS-Logs-SubscriptionFilter::Create";

    private String generateSubscriptionFilterName(final ResourceHandlerRequest<ResourceModel> request) {
        final String logicalIdentifier = StringUtils.defaultString(request.getLogicalResourceIdentifier(), DEFAULT_SUBSCRIPTION_FILTER_NAME_PREFIX);
        final String clientRequestToken = request.getClientRequestToken();

        if (request.getStackId() != null) {
            return IdentifierUtils.generateResourceIdentifier(request.getStackId(), logicalIdentifier,
                    clientRequestToken, PHYSICAL_RESOURCE_ID_MAX_LENGTH);
        } else {
            return IdentifierUtils.generateResourceIdentifier(logicalIdentifier, clientRequestToken, PHYSICAL_RESOURCE_ID_MAX_LENGTH);
        }
    }

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final Logger logger) {
        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        if (StringUtils.isBlank(model.getFilterName())) {
            final String resourceIdentifier = generateSubscriptionFilterName(request);
            model.setFilterName(resourceIdentifier);
            logger.log(String.format("Filter name not present. Generated: %s as FilterName for stackID: %s", resourceIdentifier, request.getStackId()));
        }

        final String stackId = request.getStackId() == null ? "" : request.getStackId();
        logger.log(String.format("Invoking request for: %s for stack: %s", callGraphString, stackId));

        return proxy.initiate(callGraphString, proxyClient, model, callbackContext)
                .translateToServiceRequest(Translator::translateToCreateRequest)
                .makeServiceCall((r, c) -> createResource(model, r, c, stackId))
                .success();
    }

    private PutSubscriptionFilterResponse createResource(
            final ResourceModel model,
            final PutSubscriptionFilterRequest awsRequest,
            final ProxyClient<CloudWatchLogsClient> proxyClient,
            final String stackId) {
        try {
            final boolean exists = doesResourceExist(proxyClient, model);
            if (exists) {
                logger.log(String.format("Resource exists; not creating resource for model: %s in stack %s", model, stackId));
                throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
            }

            logger.log(String.format("Resource doesn't exist. Creating a new one for model %s in stack %s", model, stackId));
            return proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::putSubscriptionFilter);
        } catch (final InvalidParameterException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
        } catch (final LimitExceededException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnServiceLimitExceededException(e);
        } catch (final OperationAbortedException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnResourceConflictException(e);
        } catch (final ServiceUnavailableException e) {
            logExceptionDetails(e, logger, stackId);
            throw new CfnServiceInternalErrorException(e);
        }
    }

}
