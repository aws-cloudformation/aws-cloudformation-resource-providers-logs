package software.amazon.logs.loggroup;

import com.google.common.collect.Sets;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DisassociateKmsKeyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.AssociateKmsKeyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final ResourceModel previousModel = request.getPreviousResourceState();
        final boolean retentionChanged = ! retentionUnchanged(previousModel, model);
        final boolean kmsKeyChanged = ! kmsKeyUnchanged(previousModel, model);
        final boolean tagsChanged = ! tagsUnchanged(previousModel, model);
        if (retentionChanged && model.getRetentionInDays() == null) {
            deleteRetentionPolicy(proxy, request, logger);
        } else if (retentionChanged){
            putRetentionPolicy(proxy, request, logger);
        }

        // It can take up to five minutes for the (dis)associate operation to take effect
        // It's unclear from the documentation if that state can be checked via the API.
        // https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/encrypt-log-data-kms.html
        if (kmsKeyChanged && model.getKmsKeyId() == null) {
            disassociateKmsKey(proxy, request, logger);
        } else if (kmsKeyChanged) {
            associateKmsKey(proxy, request, logger);
        }

        if (tagsChanged) {
            updateTags(proxy, previousModel, model, logger);
        }

        return ProgressEvent.defaultSuccessHandler(model);
    }

    private void deleteRetentionPolicy(final AmazonWebServicesClientProxy proxy,
                                       final ResourceHandlerRequest<ResourceModel> request,
                                       final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();
        final DeleteRetentionPolicyRequest deleteRetentionPolicyRequest =
            Translator.translateToDeleteRetentionPolicyRequest(model);
        try {
            proxy.injectCredentialsAndInvokeV2(deleteRetentionPolicyRequest,
                ClientBuilder.getClient()::deleteRetentionPolicy);
        } catch (final ResourceNotFoundException e) {
            throwNotFoundException(model);
        }

        final String retentionPolicyMessage =
            String.format("%s [%s] successfully deleted retention policy.",
                ResourceModel.TYPE_NAME, model.getLogGroupName());
        logger.log(retentionPolicyMessage);
    }

    private void putRetentionPolicy(final AmazonWebServicesClientProxy proxy,
                                    final ResourceHandlerRequest<ResourceModel> request,
                                    final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();
        final PutRetentionPolicyRequest putRetentionPolicyRequest =
            Translator.translateToPutRetentionPolicyRequest(model);
        try {
            proxy.injectCredentialsAndInvokeV2(putRetentionPolicyRequest,
                ClientBuilder.getClient()::putRetentionPolicy);
        } catch (final ResourceNotFoundException e) {
            throwNotFoundException(model);
        }

        final String retentionPolicyMessage =
            String.format("%s [%s] successfully applied retention in days: [%d].",
                ResourceModel.TYPE_NAME, model.getLogGroupName(), model.getRetentionInDays());
        logger.log(retentionPolicyMessage);
    }

    private void disassociateKmsKey(final AmazonWebServicesClientProxy proxy,
                                    final ResourceHandlerRequest<ResourceModel> request,
                                    final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();
        final DisassociateKmsKeyRequest disassociateKmsKeyRequest =
                Translator.translateToDisassociateKmsKeyRequest(model);
        try {
            proxy.injectCredentialsAndInvokeV2(disassociateKmsKeyRequest,
                    ClientBuilder.getClient()::disassociateKmsKey);
        } catch (final ResourceNotFoundException e) {
            // The specified resource does not exist.
            throwNotFoundException(model);
        } catch (final InvalidParameterException e) {
            // A parameter is specified incorrectly. We should be passing valid parameters.
            throw new CfnInternalFailureException(e);
        } catch (final OperationAbortedException e){
            // Multiple requests to update the same resource were in conflict.
            throw new CfnResourceConflictException(ResourceModel.TYPE_NAME,
                Objects.toString(model.getPrimaryIdentifier()), "OperationAborted", e);
        } catch (final ServiceUnavailableException e) {
            // The service cannot complete the request.
            throw new CfnServiceInternalErrorException(e);
        }

        final String kmsKeyMessage =
                String.format("%s [%s] successfully disassociated kms key.",
                        ResourceModel.TYPE_NAME, model.getLogGroupName());
        logger.log(kmsKeyMessage);
    }

    private void associateKmsKey(final AmazonWebServicesClientProxy proxy,
                                 final ResourceHandlerRequest<ResourceModel> request,
                                 final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();
        final AssociateKmsKeyRequest associateKmsKeyRequest =
                Translator.translateToAssociateKmsKeyRequest(model);
        try {
            proxy.injectCredentialsAndInvokeV2(associateKmsKeyRequest,
                    ClientBuilder.getClient()::associateKmsKey);
        } catch (final ResourceNotFoundException e) {
            // The specified resource does not exist.
            throwNotFoundException(model);
        } catch (final InvalidParameterException e) {
            // A parameter is specified incorrectly. We should be passing valid parameters.
            throw new CfnInternalFailureException(e);
        } catch (final OperationAbortedException e){
            // Multiple requests to update the same resource were in conflict.
            throw new CfnResourceConflictException(ResourceModel.TYPE_NAME,
                Objects.toString(model.getPrimaryIdentifier()), "OperationAborted", e);
        } catch (final ServiceUnavailableException e) {
            // The service cannot complete the request.
            throw new CfnServiceInternalErrorException(e);
        }

        final String kmsKeyMessage =
                String.format("%s [%s] successfully associated kms key: [%s].",
                        ResourceModel.TYPE_NAME, model.getLogGroupName(), model.getKmsKeyId());
        logger.log(kmsKeyMessage);
    }

    private void updateTags(final AmazonWebServicesClientProxy proxy,
                            final ResourceModel previousModel,
                            final ResourceModel model,
                            final Logger logger) {
        final Set<Tag> previousTags = Optional.ofNullable(previousModel).map(ResourceModel::getTags).orElse(new HashSet<>());
        final Set<Tag> newTags = Optional.ofNullable(model.getTags()).orElse(new HashSet<>());
        final Set<Tag> tagsToRemove = Sets.difference(previousTags, newTags);
        final Set<Tag> tagsToAdd = Sets.difference(newTags, previousTags);
        try {
            if (!tagsToRemove.isEmpty()) {
                final List<String> tagKeys = tagsToRemove.stream().map(Tag::getKey).collect(Collectors.toList());
                proxy.injectCredentialsAndInvokeV2(Translator.translateToUntagLogGroupRequest(model.getLogGroupName(), tagKeys),
                        ClientBuilder.getClient()::untagLogGroup);

                final String message =
                        String.format("%s [%s] successfully removed tags: [%s]",
                                ResourceModel.TYPE_NAME, model.getLogGroupName(), tagKeys);
                logger.log(message);
            }
            if(!tagsToAdd.isEmpty()) {
                proxy.injectCredentialsAndInvokeV2(Translator.translateToTagLogGroupRequest(model.getLogGroupName(), tagsToAdd),
                        ClientBuilder.getClient()::tagLogGroup);

                final String message =
                        String.format("%s [%s] successfully added tags: [%s]",
                                ResourceModel.TYPE_NAME, model.getLogGroupName(), tagsToAdd);
                logger.log(message);
            }
        } catch (final ResourceNotFoundException e) {
            throwNotFoundException(model);
        } catch (final InvalidParameterException e) {
            throw new CfnInternalFailureException(e);
        }
    }

    private void throwNotFoundException(final ResourceModel model) {
        throw new software.amazon.cloudformation.exceptions.ResourceNotFoundException(ResourceModel.TYPE_NAME,
            Objects.toString(model.getPrimaryIdentifier()));
    }


    private static boolean retentionUnchanged(final ResourceModel previousModel, final ResourceModel model) {
        return (previousModel != null && Objects.equals(model.getRetentionInDays(), previousModel.getRetentionInDays()));
    }

    private static boolean kmsKeyUnchanged(final ResourceModel previousModel, final ResourceModel model) {
        return (previousModel != null && Objects.equals(model.getKmsKeyId(), previousModel.getKmsKeyId()));
    }

    private static boolean tagsUnchanged(final ResourceModel previousModel, final ResourceModel model) {
        if (previousModel == null && model.getTags() == null) {
            return true;
        }
        return (previousModel != null && Objects.equals(model.getTags(), previousModel.getTags()));
    }
}
