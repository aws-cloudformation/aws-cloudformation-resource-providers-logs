package com.aws.logs.metricfilter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.MetricFilter;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class TranslatorTest {

    @Test
    public void translate_packageModel() {
        final software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation metricTransformation =
                software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation.builder()
                        .defaultValue(1.0)
                        .metricName("MetricName")
                        .metricNamespace("MyNamespace")
                        .metricValue("Value")
                        .build();
        final MetricTransformation rpdkMetricTransformation = MetricTransformation.builder()
                        .defaultValue(1.0)
                        .metricName("MetricName")
                        .metricNamespace("MyNamespace")
                        .metricValue("Value")
                        .build();
        assertThat(Translator.translate(rpdkMetricTransformation)).isEqualToComparingFieldByField(metricTransformation);
    }

    @Test
    public void translate_nullPackageModel_returnsNull() {
        assertThat(Translator.translate((MetricTransformation)null)).isNull();
    }

    @Test
    public void translate_nullSDKModel_returnsNull() {
        assertThat(Translator.translate((software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation)null)).isNull();
    }

    @Test
    public void translate_SDKModel() {
        final software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation metricTransformation =
                software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation.builder()
                        .defaultValue(1.0)
                        .metricName("MetricName")
                        .metricNamespace("MyNamespace")
                        .metricValue("Value")
                        .build();
        final MetricTransformation rpdkMetricTransformation = MetricTransformation.builder()
                .defaultValue(1.0)
                .metricName("MetricName")
                .metricNamespace("MyNamespace")
                .metricValue("Value")
                .build();
        assertThat(Translator.translate(metricTransformation)).isEqualToComparingFieldByField(rpdkMetricTransformation);
    }

    @Test
    public void translateToSDK() {
        final List<software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation> metricTransformations =
                Arrays.asList(software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation.builder()
                        .defaultValue(1.0)
                        .metricName("MetricName")
                        .metricNamespace("MyNamespace")
                        .metricValue("Value")
                        .build());
        final List<MetricTransformation> rpdkMetricTransformations =
                Arrays.asList(MetricTransformation.builder()
                        .defaultValue(1.0)
                        .metricName("MetricName")
                        .metricNamespace("MyNamespace")
                        .metricValue("Value")
                        .build());
        assertThat(Translator.translateToSDK(rpdkMetricTransformations)).containsAll(metricTransformations);
    }

    @Test
    public void translateToSDK_emptyList_returnsNull() {
        assertThat(Translator.translateToSDK(Collections.emptyList())).isNull();
    }

    @Test
    public void translatefromSDK() {
        final List<software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation> metricTransformations =
                Arrays.asList(software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation.builder()
                        .defaultValue(1.0)
                        .metricName("MetricName")
                        .metricNamespace("MyNamespace")
                        .metricValue("Value")
                        .build());
        final List<MetricTransformation> rpdkMetricTransformations =
                Arrays.asList(MetricTransformation.builder()
                        .defaultValue(1.0)
                        .metricName("MetricName")
                        .metricNamespace("MyNamespace")
                        .metricValue("Value")
                        .build());
        assertThat(Translator.translateFromSDK(metricTransformations)).containsAll(rpdkMetricTransformations);
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
    public void extractMetricFilters_API_removesEmptyFilterPattern() {
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
                .filterPattern("")
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

    @Test
    public void translateToDeleteRequest() {
        final List<MetricTransformation> rpdkMetricTransformations =
                Arrays.asList(MetricTransformation.builder()
                        .defaultValue(1.0)
                        .metricName("MetricName")
                        .metricNamespace("MyNamespace")
                        .metricValue("Value")
                        .build());
        final ResourceModel model = ResourceModel.builder()
                .logGroupName("LogGroup")
                .metricTransformations(rpdkMetricTransformations)
                .filterPattern("Pattern")
                .filterName("FilterName")
                .build();

        final DeleteMetricFilterRequest expectedRequest = DeleteMetricFilterRequest.builder()
                .filterName("FilterName")
                .logGroupName("LogGroup")
                .build();

        final DeleteMetricFilterRequest actualRequest = Translator.translateToDeleteRequest(model);

        assertThat(actualRequest).isEqualToComparingFieldByField(expectedRequest);
    }

    @Test
    public void translateToPutRequest() {
        final List<MetricTransformation> rpdkMetricTransformations =
                Arrays.asList(MetricTransformation.builder()
                        .defaultValue(1.0)
                        .metricName("MetricName")
                        .metricNamespace("MyNamespace")
                        .metricValue("Value")
                        .build());
        final List<software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation> metricTransformations =
                Arrays.asList(software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation.builder()
                        .defaultValue(1.0)
                        .metricName("MetricName")
                        .metricNamespace("MyNamespace")
                        .metricValue("Value")
                        .build());

        final ResourceModel model = ResourceModel.builder()
                .logGroupName("LogGroup")
                .metricTransformations(rpdkMetricTransformations)
                .filterPattern("Pattern")
                .filterName("FilterName")
                .build();

        final PutMetricFilterRequest expectedRequest = PutMetricFilterRequest.builder()
                .logGroupName("LogGroup")
                .metricTransformations(metricTransformations)
                .filterPattern("Pattern")
                .filterName("FilterName")
                .build();

        final DeleteMetricFilterRequest actualRequest = Translator.translateToDeleteRequest(model);

        assertThat(actualRequest).isEqualToComparingFieldByField(expectedRequest);
    }

    @Test
    public void translateToReadRequest() {
        final List<MetricTransformation> rpdkMetricTransformations =
                Arrays.asList(MetricTransformation.builder()
                        .defaultValue(1.0)
                        .metricName("MetricName")
                        .metricNamespace("MyNamespace")
                        .metricValue("Value")
                        .build());
        final ResourceModel model = ResourceModel.builder()
                .logGroupName("LogGroup")
                .metricTransformations(rpdkMetricTransformations)
                .filterPattern("Pattern")
                .filterName("FilterName")
                .build();

        final DescribeMetricFiltersRequest expectedRequest = DescribeMetricFiltersRequest.builder()
                .logGroupName("LogGroup")
                .filterNamePrefix("FilterName")
                .limit(1)
                .build();

        final DescribeMetricFiltersRequest actualRequest = Translator.translateToReadRequest(model);

        assertThat(actualRequest).isEqualToComparingFieldByField(expectedRequest);
    }

    @Test
    public void translateToListRequest() {
        final DescribeMetricFiltersRequest expectedRequest = DescribeMetricFiltersRequest.builder()
                .limit(50)
                .nextToken("token")
                .build();

        final DescribeMetricFiltersRequest actualRequest = Translator.translateToListRequest(50, "token");

        assertThat(actualRequest).isEqualToComparingFieldByField(expectedRequest);
    }
}
