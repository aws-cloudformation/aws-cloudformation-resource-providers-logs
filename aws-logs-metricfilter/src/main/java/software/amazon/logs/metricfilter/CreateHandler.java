package software.amazon.logs.metricfilter;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

public class CreateHandler extends BaseHandlerStd {
    // if you change the value in the line below, please also update the resource schema
    private static final int MAX_LENGTH_METRIC_FILTER_NAME = 512;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        // resource can auto-generate a name if not supplied by caller.
        // this logic should move up into the CloudFormation engine, but
        // currently exists here for backwards-compatibility with existing models
        if (StringUtils.isNullOrEmpty(model.getFilterName())) {
            model.setFilterName(
                    IdentifierUtils.generateResourceIdentifier(
                            request.getLogicalResourceIdentifier(),
                            request.getClientRequestToken(),
                            MAX_LENGTH_METRIC_FILTER_NAME
                    )
            );
        }
        try {
            boolean exists = exists(proxyClient, model);
            if (exists) {
                CfnAlreadyExistsException error = new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, model.getPrimaryIdentifier().toString());
                return ProgressEvent.defaultFailureHandler(error, Translator.translateToErrorCode(error));
            }
            final PutMetricFilterRequest putRequest = Translator.translateToCreateRequest(model);

            proxyClient.injectCredentialsAndInvokeV2(putRequest, proxyClient.client()::putMetricFilter);
            logger.log(String.format("%s successfully created.", ResourceModel.TYPE_NAME));
            return ProgressEvent.defaultSuccessHandler(model);
        } catch (final AwsServiceException e) {
            BaseHandlerException error = Translator.translateException(e);
            return ProgressEvent.defaultFailureHandler(error, Translator.translateToErrorCode(error));

        }
    }
}
