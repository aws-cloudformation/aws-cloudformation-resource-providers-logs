package software.amazon.logs.loggroup;

import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DisassociateKmsKeyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.AssociateKmsKeyRequest;

import java.util.Objects;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        // Everything except RetentionPolicyInDays and KmsKeyId is createOnly
        final ResourceModel model = request.getDesiredResourceState();
        final ResourceModel previousModel = request.getPreviousResourceState();
        final boolean retentionChanged = ! retentionUnchanged(previousModel, model);
        final boolean kmsKeyChanged = ! kmsKeyUnchanged(previousModel, model);
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
            throwNotFoundException(model);
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
            throwNotFoundException(model);
        }

        final String kmsKeyMessage =
                String.format("%s [%s] successfully associated kms key: [%s].",
                        ResourceModel.TYPE_NAME, model.getLogGroupName(), model.getKmsKeyId());
        logger.log(kmsKeyMessage);
    }

    private void throwNotFoundException(final ResourceModel model) {
        throw new software.amazon.cloudformation.exceptions.ResourceNotFoundException(ResourceModel.TYPE_NAME,
            Objects.toString(model.getPrimaryIdentifier()));
    }


    private static boolean retentionUnchanged(final ResourceModel previousModel, final ResourceModel model) {
        return (previousModel != null && model.getRetentionInDays().equals(previousModel.getRetentionInDays()));
    }

    private static boolean kmsKeyUnchanged(final ResourceModel previousModel, final ResourceModel model) {
        return (previousModel != null && model.getKmsKeyId().equals(previousModel.getKmsKeyId()));
    }
}
