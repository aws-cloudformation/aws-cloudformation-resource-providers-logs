package com.aws.logs.metricfilter;

import com.aws.cfn.proxy.AmazonWebServicesClientProxy;
import com.aws.cfn.proxy.Logger;
import com.aws.cfn.proxy.ProgressEvent;
import com.aws.cfn.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterResponse;

import java.util.UUID;

import static com.aws.logs.metricfilter.ClientBuilder.getCloudWatchLogsClient;

public class CreateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        try {
            if (model.getFilterName() == null) {
                model.setFilterName(UUID.randomUUID().toString());
            }

            final PutMetricFilterRequest putMetricFilterRequest = PutMetricFilterRequest
                    .builder()
                    .logGroupName(model.getLogGroupName())
                    .filterName(model.getFilterName())
                    .filterPattern(model.getFilterPattern())
                    .metricTransformations(Utils.translateMetricTransformations(model))
                    .build();
            final PutMetricFilterResponse response = proxy.injectCredentialsAndInvokeV2(putMetricFilterRequest, getCloudWatchLogsClient()::putMetricFilter);
            logger.log(String.format("Successfully create AWS::Logs::MetricFilter of {%s} with Request Id %s and Client Token %s", model.getFilterName(), response.responseMetadata().requestId(), request.getClientRequestToken()));
            return Utils.defaultSuccessHandler(model);
        } catch (Exception e) {
            logger.log(String.format("Failed to create AWS::Logs::MetricFilter of {%s}, caused by Exception {%s} with Client Token %s", model.getFilterName(), e.toString(), request.getClientRequestToken()));
            return Utils.defaultFailureHandler(e, null);
        }
    }
}
