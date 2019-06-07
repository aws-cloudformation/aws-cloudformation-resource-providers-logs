package com.aws.logs.metricfilter;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

public class Translator {

    public static List<software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation> translateToSDK(
        final List<com.aws.logs.metricfilter.MetricTransformation> in) {
        if (CollectionUtils.isEmpty(in)) return null;

        return in.stream().map(Translator::translate).collect(Collectors.toList());
    }

    public static software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation translate(
        final com.aws.logs.metricfilter.MetricTransformation in) {
        if (in == null) return null;

        return software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation.builder()
            .defaultValue(in.getDefaultValue())
            .metricName(in.getMetricName())
            .metricNamespace(in.getMetricNamespace())
            .metricValue(in.getMetricValue())
            .build();
    }

    public static List<com.aws.logs.metricfilter.MetricTransformation> translateFromSDK(
        final List<software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation>in) {
        if (CollectionUtils.isEmpty(in)) return null;

        return in.stream().map(Translator::translate).collect(Collectors.toList());
    }

    public static com.aws.logs.metricfilter.MetricTransformation translate(
        final software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation in) {
        if (in == null) return null;

        return com.aws.logs.metricfilter.MetricTransformation.builder()
            .defaultValue(in.defaultValue())
            .metricName(in.metricName())
            .metricNamespace(in.metricNamespace())
            .metricValue(in.metricValue())
            .build();
    }
}
