package software.amazon.logs.metricfilter;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Objects;

public class ReadHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<CloudWatchLogsClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final DescribeMetricFiltersRequest describeMetricFiltersRequest = Translator.translateToReadRequest(model);
        DescribeMetricFiltersResponse awsResponse;
        try {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(describeMetricFiltersRequest, proxyClient.client()::describeMetricFilters);
        } catch (final AwsServiceException e) {
            BaseHandlerException error = Translator.translateException(e);
            return ProgressEvent.defaultFailureHandler(error, Translator.translateToErrorCode(error));
        }

        if (awsResponse.metricFilters().isEmpty()) {
            logger.log(String.format("Resource with id %s does not exist." , model.getPrimaryIdentifier()));
            BaseHandlerException error = new CfnNotFoundException(ResourceModel.TYPE_NAME,
                    Objects.toString(model.getPrimaryIdentifier()));
            return ProgressEvent.defaultFailureHandler(error, Translator.translateToErrorCode(error));
        }
        ResourceModel readModel = Translator.translateFromReadResponse(awsResponse);

        logger.log(String.format("%s has successfully been read." , ResourceModel.TYPE_NAME));
        return ProgressEvent.defaultSuccessHandler(readModel);
    }
}
