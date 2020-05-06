package software.amazon.logs.metricfilter;

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
    private static final software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation METRIC_TRANSFORMATION =
            software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation.builder()
                    .defaultValue(1.0)
                    .metricName("MetricName")
                    .metricNamespace("MyNamespace")
                    .metricValue("Value")
                    .build();

    private static final MetricTransformation RPDK_METRIC_TRANSFORMATION =
            MetricTransformation.builder()
                    .defaultValue(1.0)
                    .metricName("MetricName")
                    .metricNamespace("MyNamespace")
                    .metricValue("Value")
                    .build();

    private static final MetricFilter METRIC_FILTER = MetricFilter.builder()
            .filterName("Filter")
            .logGroupName("LogGroup")
            .filterPattern("Pattern")
            .metricTransformations(Collections.singletonList(METRIC_TRANSFORMATION))
            .build();

    private static final ResourceModel RESOURCE_MODEL = ResourceModel.builder()
            .logGroupName("LogGroup")
            .metricTransformations(Collections.singletonList(RPDK_METRIC_TRANSFORMATION))
            .filterPattern("Pattern")
            .filterName("FilterName")
            .build();

    @Test
    public void translate_packageModel() {
        assertThat(Translator.translateMetricTransformationToSdk(RPDK_METRIC_TRANSFORMATION))
                .isEqualToComparingFieldByField(METRIC_TRANSFORMATION);
    }

    @Test
    public void translate_nullPackageModel_returnsNull() {
        assertThat(Translator.translateMetricTransformationToSdk((MetricTransformation)null)).isNull();
    }

    @Test
    public void translate_nullSDKModel_returnsNull() {
        assertThat(Translator.translateMetricTransformationToSdk((software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation)null)).isNull();
    }

    @Test
    public void translate_SDKModel() {
        assertThat(Translator.translateMetricTransformationToSdk(METRIC_TRANSFORMATION))
                .isEqualToComparingFieldByField(RPDK_METRIC_TRANSFORMATION);
    }

    @Test
    public void translateToSDK() {
        assertThat(Translator.translateMetricTransformationToSDK(Collections.singletonList(RPDK_METRIC_TRANSFORMATION)))
                .containsExactly(METRIC_TRANSFORMATION);
    }

    @Test
    public void translateFromSDK() {
        assertThat(Translator.translateMetricTransformationFromSdk(Collections.singletonList(METRIC_TRANSFORMATION)))
                .containsExactly(RPDK_METRIC_TRANSFORMATION);
    }

    @Test
    public void translateFromSDK_emptyList_returnsNull() {
        assertThat(Translator.translateMetricTransformationFromSdk(Collections.emptyList())).isNull();
    }

    @Test
    public void extractMetricFilters_success() {
        final DescribeMetricFiltersResponse response = DescribeMetricFiltersResponse.builder()
                .metricFilters(Collections.singletonList(METRIC_FILTER))
                .build();

        final List<ResourceModel> expectedModels = Arrays.asList(ResourceModel.builder()
                .filterName("Filter")
                .logGroupName("LogGroup")
                .filterPattern("Pattern")
                .metricTransformations(Collections.singletonList(RPDK_METRIC_TRANSFORMATION))
                .build());

        assertThat(Translator.translateFromListResponse(response)).isEqualTo(expectedModels);
    }

    @Test
    public void extractMetricFilters_API_removesEmptyFilterPattern() {
        final DescribeMetricFiltersResponse response = DescribeMetricFiltersResponse.builder()
                .metricFilters(Collections.singletonList(METRIC_FILTER.toBuilder()
                        .filterPattern(null)
                        .build()))
                .build();
        final List<ResourceModel> expectedModels = Arrays.asList(ResourceModel.builder()
                .filterName("Filter")
                .logGroupName("LogGroup")
                .filterPattern("")
                .metricTransformations(Collections.singletonList(RPDK_METRIC_TRANSFORMATION))
                .build());

        assertThat(Translator.translateFromListResponse(response)).isEqualTo(expectedModels);
    }

    @Test
    public void extractMetricFilters_noFilters() {
        final DescribeMetricFiltersResponse response = DescribeMetricFiltersResponse.builder()
                .metricFilters(Collections.emptyList())
                .build();
        final List<ResourceModel> expectedModels = Collections.emptyList();

        assertThat(Translator.translateFromListResponse(response)).isEqualTo(expectedModels);
    }

    @Test
    public void translateToDeleteRequest() {
        final DeleteMetricFilterRequest expectedRequest = DeleteMetricFilterRequest.builder()
                .filterName("FilterName")
                .logGroupName("LogGroup")
                .build();

        final DeleteMetricFilterRequest actualRequest = Translator.translateToDeleteRequest(RESOURCE_MODEL);

        assertThat(actualRequest).isEqualToComparingFieldByField(expectedRequest);
    }

    @Test
    public void translateToPutRequest() {
        final PutMetricFilterRequest expectedRequest = PutMetricFilterRequest.builder()
                .logGroupName("LogGroup")
                .metricTransformations(Collections.singletonList(METRIC_TRANSFORMATION))
                .filterPattern("Pattern")
                .filterName("FilterName")
                .build();

        final DeleteMetricFilterRequest actualRequest = Translator.translateToDeleteRequest(RESOURCE_MODEL);

        assertThat(actualRequest).isEqualToComparingFieldByField(expectedRequest);
    }

    @Test
    public void translateToReadRequest() {
        final DescribeMetricFiltersRequest expectedRequest = DescribeMetricFiltersRequest.builder()
                .logGroupName("LogGroup")
                .filterNamePrefix("FilterName")
                .limit(1)
                .build();

        final DescribeMetricFiltersRequest actualRequest = Translator.translateToReadRequest(RESOURCE_MODEL);

        assertThat(actualRequest).isEqualToComparingFieldByField(expectedRequest);
    }

    @Test
    public void translateToListRequest() {
        final DescribeMetricFiltersRequest expectedRequest = DescribeMetricFiltersRequest.builder()
                .limit(50)
                .nextToken("token")
                .build();

        final DescribeMetricFiltersRequest actualRequest = Translator.translateToListRequest( "token");

        assertThat(actualRequest).isEqualToComparingFieldByField(expectedRequest);
    }

}
