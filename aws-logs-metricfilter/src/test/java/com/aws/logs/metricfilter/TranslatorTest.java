package com.aws.logs.metricfilter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.MetricFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class TranslatorTest {

    @Test
    public void translate_nullPackageModel_returnsNull() {
        assertThat(Translator.translate((MetricTransformation)null)).isNull();
    }

    @Test
    public void translate_nullSDKModel_returnsNull() {
        assertThat(Translator.translate((software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation)null)).isNull();
    }

    @Test
    public void translateToSDK_emptyList_returnsNull() {
        assertThat(Translator.translateToSDK(Collections.emptyList())).isNull();
    }

    @Test
    public void translateFromSDK_emptyList_returnsNull() {
        assertThat(Translator.translateFromSDK(Collections.emptyList())).isNull();
    }

    @Test
    public void extractMetricFilters_success() {
        final List<software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation> metricTransformations =
                Arrays.asList(software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation.builder()
                        .defaultValue(1.0)
                        .metricName("MetricName")
                        .metricNamespace("MyNamespace")
                        .metricValue("Value")
                        .build());
        final List<MetricFilter> metricFilters = Arrays.asList(MetricFilter.builder()
                .filterName("Filter")
                .logGroupName("LogGroup")
                .filterPattern("Pattern")
                .metricTransformations(metricTransformations)
                .build());
        final DescribeMetricFiltersResponse response = DescribeMetricFiltersResponse.builder()
                .metricFilters(metricFilters)
                .build();

        final List<MetricTransformation> rpdkMetricTransformations =
                Arrays.asList(MetricTransformation.builder()
                        .defaultValue(1.0)
                        .metricName("MetricName")
                        .metricNamespace("MyNamespace")
                        .metricValue("Value")
                        .build());
        final List<ResourceModel> expectedModels = Arrays.asList(ResourceModel.builder()
                .filterName("Filter")
                .logGroupName("LogGroup")
                .filterPattern("Pattern")
                .metricTransformations(rpdkMetricTransformations)
                .build());

        assertThat(Translator.translateFromSDK(response)).isEqualTo(expectedModels);
    }

    @Test
    public void extractMetricFilters_noFilters() {
        final DescribeMetricFiltersResponse response = DescribeMetricFiltersResponse.builder()
                .metricFilters(Collections.emptyList())
                .build();
        final List<ResourceModel> expectedModels = Collections.emptyList();

        assertThat(Translator.translateFromSDK(response)).isEqualTo(expectedModels);

    }
}
