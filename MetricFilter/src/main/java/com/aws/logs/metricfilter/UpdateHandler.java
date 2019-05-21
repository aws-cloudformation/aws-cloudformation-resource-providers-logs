package com.aws.logs.metricfilter;

import com.aws.cfn.proxy.AmazonWebServicesClientProxy;
import com.aws.cfn.proxy.Logger;
import com.aws.cfn.proxy.ProgressEvent;
import com.aws.cfn.proxy.OperationStatus;
import com.aws.cfn.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterResponse;

import static com.aws.logs.metricfilter.ClientBuilder.getCloudWatchLogsClient;

public class UpdateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel currentModel = request.getDesiredResourceState();
        final ResourceModel previousModel = request.getPreviousResourceState();

        try {
            currentModel.setFilterName(previousModel.getFilterName());

            final PutMetricFilterRequest putMetricFilterRequest = PutMetricFilterRequest
                    .builder()
                    .logGroupName(currentModel.getLogGroupName())
                    .filterName(previousModel.getFilterName())
                    .filterPattern(currentModel.getFilterPattern())
                    .metricTransformations(Utils.translateMetricTransformations(currentModel))
                    .build();
            PutMetricFilterResponse response = proxy.injectCredentialsAndInvokeV2(putMetricFilterRequest, getCloudWatchLogsClient()::putMetricFilter);
            logger.log(String.format("Successfully update AWS::Logs::MetricFilter of {%s} with Request Id %s and ClientToken %s", currentModel, response.responseMetadata().requestId(), request.getClientRequestToken()));
            return Utils.defaultSuccessHandler(currentModel);
        } catch (Exception e) {
            logger.log(String.format("Failed to update AWS::Logs::MetricFilter to model {%s} from model {%s}, caused by Exception {%s} with ClientToken %s", currentModel, previousModel, e.toString(), request.getClientRequestToken()));
            return Utils.defaultFailureHandler(e, null);
        }
    }
}
