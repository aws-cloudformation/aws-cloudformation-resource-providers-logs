package com.aws.logs.metricfilter;

import org.apache.commons.collections.CollectionUtils;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.MetricFilter;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;

import java.util.List;
import java.util.stream.Collectors;

final class Translator {
    private Translator() {
        throw new AssertionError("Attempted to instantiate a utility class.");
    }

    static List<software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation> translateToSDK(
        final List<com.aws.logs.metricfilter.MetricTransformation> in) {
        if (CollectionUtils.isEmpty(in)) return null;

        return in.stream().map(Translator::translate).collect(Collectors.toList());
    }

    static software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation translate(
        final com.aws.logs.metricfilter.MetricTransformation in) {
        if (in == null) return null;

        return software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation.builder()
            .defaultValue(in.getDefaultValue())
            .metricName(in.getMetricName())
            .metricNamespace(in.getMetricNamespace())
            .metricValue(in.getMetricValue())
            .build();
    }

    static List<com.aws.logs.metricfilter.MetricTransformation> translateFromSDK(
        final List<software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation> in) {
        if (CollectionUtils.isEmpty(in)) return null;

        return in.stream().map(Translator::translate).collect(Collectors.toList());
    }

    static com.aws.logs.metricfilter.MetricTransformation translate(
        final software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation in) {
        if (in == null) return null;

        return com.aws.logs.metricfilter.MetricTransformation.builder()
            .defaultValue(in.defaultValue())
            .metricName(in.metricName())
            .metricNamespace(in.metricNamespace())
            .metricValue(in.metricValue())
            .build();
    }

    static List<ResourceModel> translateFromSDK(final DescribeMetricFiltersResponse describeMetricFiltersResponse) {
        return describeMetricFiltersResponse.metricFilters()
            .stream()
            .map(Translator::translate)
            .collect(Collectors.toList());
    }

    static ResourceModel translate(final MetricFilter metricFilter) {
        // When a filter pattern is "" the API sets it to null, but this is a meaningful pattern and the
        // contract should be identical to what our caller provided
        // per https://w.amazon.com/index.php/AWS21/Design/Uluru/HandlerContract
        //
        // MetricFilter is also a required property, so we assume that if it is null, it is because the
        // pattern is actually an empty string. An empty string indicates that the filter should include
        // everything, so it is akin to having no filter:
        // - https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/FilterAndPatternSyntax.html
        return ResourceModel.builder()
            .filterName(metricFilter.filterName())
            .filterPattern(metricFilter.filterPattern() == null ? "" : metricFilter.filterPattern())
            .logGroupName(metricFilter.logGroupName())
            .metricTransformations(translateFromSDK(metricFilter.metricTransformations()))
            .build();
    }

    static DeleteMetricFilterRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteMetricFilterRequest.builder()
            .filterName(model.getFilterName())
            .logGroupName(model.getLogGroupName())
            .build();
    }

    static DescribeMetricFiltersRequest translateToReadRequest(final ResourceModel model) {
        return DescribeMetricFiltersRequest.builder()
            .filterNamePrefix(model.getFilterName())
            .logGroupName(model.getLogGroupName())
            .limit(1)
            .build();
    }

    static DescribeMetricFiltersRequest translateToListRequest(final int limit, final String nextToken) {
        return DescribeMetricFiltersRequest.builder()
            .nextToken(nextToken)
            .limit(limit)
            .build();
    }

    static PutMetricFilterRequest translateToPutRequest(final ResourceModel model) {
        return PutMetricFilterRequest.builder()
            .filterName(model.getFilterName())
            .filterPattern(model.getFilterPattern())
            .logGroupName(model.getLogGroupName())
            .metricTransformations(translateToSDK(model.getMetricTransformations()))
            .build();
    }

    static String buildResourceAlreadyExistsErrorMessage(final String resourceIdentifier) {
        return String.format("Resource of type '%s' with identifier '%s' already exists.",
            ResourceModel.TYPE_NAME,
            resourceIdentifier);
    }

    static String buildResourceDoesNotExistErrorMessage(final String resourceIdentifier) {
        return String.format("Resource of type '%s' with identifier '%s' was not found.",
            ResourceModel.TYPE_NAME,
            resourceIdentifier);
    }
}
