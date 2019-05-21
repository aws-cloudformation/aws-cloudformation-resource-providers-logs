package com.aws.logs.metricfilter;

import com.aws.cfn.proxy.AmazonWebServicesClientProxy;
import com.aws.cfn.proxy.Logger;
import com.aws.cfn.proxy.ProgressEvent;
import com.aws.cfn.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteMetricFilterResponse;

import static com.aws.logs.metricfilter.ClientBuilder.getCloudWatchLogsClient;

public class DeleteHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        try {
            final DeleteMetricFilterRequest deleteMetricFilterRequest = DeleteMetricFilterRequest
                    .builder()
                    .filterName(model.getFilterName())
                    .logGroupName(model.getLogGroupName())
                    .build();
            final DeleteMetricFilterResponse response = proxy.injectCredentialsAndInvokeV2(deleteMetricFilterRequest, getCloudWatchLogsClient()::deleteMetricFilter);
            logger.log(String.format("Successfully delete AWS::Logs::MetricFilter of {%s} with Request Id %s and Client Token %s", model, response.responseMetadata().requestId(), request.getClientRequestToken()));
            return Utils.defaultSuccessHandler(model);
        } catch (Exception e) {
            logger.log(String.format("Failed to delete AWS::Logs::MetricFilter of {%s}, caused by Exception {%s} with Client Token %s", model, e.toString(), request.getClientRequestToken()));
            return Utils.defaultFailureHandler(e, null);
        }
    }
}
