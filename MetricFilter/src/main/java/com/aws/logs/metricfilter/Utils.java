package com.aws.logs.metricfilter;

import com.aws.cfn.proxy.HandlerErrorCode;
import com.aws.cfn.proxy.OperationStatus;
import com.aws.cfn.proxy.ProgressEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation;

import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static List<MetricTransformation> translateMetricTransformations(final ResourceModel resourceModel) {
        return resourceModel.getMetricTransformations().stream().map(e -> MetricTransformation
                .builder()
                .metricName(e.getMetricName())
                .metricNamespace(e.getMetricNamespace())
                .metricValue(e.getMetricValue())
                .defaultValue(e.getDefaultValue().doubleValue())
                .build())
                .collect(Collectors.toList());
    }

    public static ProgressEvent<ResourceModel, CallbackContext> defaultFailureHandler(final Exception e, final HandlerErrorCode handlerErrorCode) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .errorCode(handlerErrorCode)
                .message(e.getMessage())
                .status(OperationStatus.FAILED)
                .build();
    }

    public static ProgressEvent<ResourceModel, CallbackContext> defaultSuccessHandler(final ResourceModel resourceModel) {
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(resourceModel)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
