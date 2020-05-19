package software.amazon.logs.metricfilter;

import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Translator {

  static software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation translateMetricTransformationToSdk
          (final software.amazon.logs.metricfilter.MetricTransformation metricTransformation) {
    if (metricTransformation == null) {
      return null;
    }
    return software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation.builder()
            .metricName(metricTransformation.getMetricName())
            .metricValue(metricTransformation.getMetricValue())
            .metricNamespace(metricTransformation.getMetricNamespace())
            .defaultValue(metricTransformation.getDefaultValue())
            .build();
  }

  static software.amazon.logs.metricfilter.MetricTransformation translateMetricTransformationFromSdk
          (final software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation metricTransformation) {
    if (metricTransformation == null) {
      return null;
    }
    return software.amazon.logs.metricfilter.MetricTransformation.builder()
            .metricName(metricTransformation.metricName())
            .metricValue(metricTransformation.metricValue())
            .metricNamespace(metricTransformation.metricNamespace())
            .defaultValue(metricTransformation.defaultValue())
            .build();
  }

  static List<software.amazon.logs.metricfilter.MetricTransformation> translateMetricTransformationFromSdk
          (final List<software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation> metricTransformations) {
    if (metricTransformations.isEmpty()) {
      return null;
    }
    return metricTransformations.stream()
            .map(Translator::translateMetricTransformationFromSdk)
            .collect(Collectors.toList());
  }

  static ResourceModel translateMetricFilter
          (final software.amazon.awssdk.services.cloudwatchlogs.model.MetricFilter metricFilter) {
    List<MetricTransformation> mts = metricFilter.metricTransformations()
            .stream()
            .map(Translator::translateMetricTransformationFromSdk)
            .collect(Collectors.toList());
    return ResourceModel.builder()
            .filterName(metricFilter.filterName())
            .logGroupName(metricFilter.logGroupName())
            // When a filter pattern is "" the API sets it to null, but this is a meaningful pattern and the
            // contract should be identical to what our caller provided
            .filterPattern(metricFilter.filterPattern() == null ? "" : metricFilter.filterPattern())
            .metricTransformations(mts)
            .build();
  }

  static software.amazon.awssdk.services.cloudwatchlogs.model.MetricFilter translateToSDK
          (final ResourceModel model) {
    List<software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation> mts = model.getMetricTransformations()
            .stream()
            .map(Translator::translateMetricTransformationToSdk)
            .collect(Collectors.toList());
    return software.amazon.awssdk.services.cloudwatchlogs.model.MetricFilter.builder()
            .filterName(model.getFilterName())
            .logGroupName(model.getLogGroupName())
            .filterPattern(model.getFilterPattern())
            .metricTransformations(mts)
            .build();
  }

  static software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation translateToSDK
          (final MetricTransformation metricTransformation) {
    return translateMetricTransformationToSdk(metricTransformation);
  }

  static List<software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation> translateMetricTransformationToSDK
          (final List<MetricTransformation> metricTransformations) {
    return metricTransformations.stream()
            .map(Translator::translateToSDK)
            .collect(Collectors.toList());
  }

  static PutMetricFilterRequest translateToCreateRequest(final ResourceModel model) {
    return PutMetricFilterRequest.builder()
            .logGroupName(model.getLogGroupName())
            .filterName(model.getFilterName())
            .filterPattern(model.getFilterPattern())
            .metricTransformations(model.getMetricTransformations()
                    .stream()
                    .map(Translator::translateMetricTransformationToSdk)
                    .collect(Collectors.toSet()))
            .build();
  }

  static DescribeMetricFiltersRequest translateToReadRequest(final ResourceModel model) {
    return DescribeMetricFiltersRequest.builder()
            .filterNamePrefix(model.getFilterName())
            .logGroupName(model.getLogGroupName())
            .limit(1)
            .build();
  }

  static ResourceModel translateFromReadResponse(final DescribeMetricFiltersResponse awsResponse) {
    return awsResponse.metricFilters()
            .stream()
            .map(Translator::translateMetricFilter)
            .findFirst()
            .get();
  }

  static DeleteMetricFilterRequest translateToDeleteRequest(final ResourceModel model) {
    return DeleteMetricFilterRequest.builder()
            .filterName(model.getFilterName())
            .logGroupName(model.getLogGroupName())
            .build();
  }

  static PutMetricFilterRequest translateToUpdateRequest(final ResourceModel model) {
    return translateToCreateRequest(model);
  }

  static DescribeMetricFiltersRequest translateToListRequest(final String nextToken) {
    return DescribeMetricFiltersRequest.builder()
            .nextToken(nextToken)
            .limit(50)
            .build();
  }

  static List<ResourceModel> translateFromListResponse(final DescribeMetricFiltersResponse awsResponse) {
    return streamOfOrEmpty(awsResponse.metricFilters())
        .map(Translator::translateMetricFilter)
        .collect(Collectors.toList());
  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
        .map(Collection::stream)
        .orElseGet(Stream::empty);
  }
}
