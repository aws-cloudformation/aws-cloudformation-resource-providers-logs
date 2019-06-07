package com.aws.logs.metricfilter;

import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.MetricFilter;
import software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

import java.util.Arrays;
import java.util.Collections;

import static com.aws.logs.metricfilter.Matchers.assertThatModelsAreEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ReadHandler handler = new ReadHandler();

        final MetricFilter filter = MetricFilter.builder()
            .filterName("test-filter")
            .logGroupName("test-lg")
            .filterPattern("[filter pattern]")
            .metricTransformations(MetricTransformation.builder().metricName("metric").metricValue("value").build())
            .build();
        final DescribeMetricFiltersResponse describeResponse = DescribeMetricFiltersResponse.builder()
            .metricFilters(filter)
            .build();

        doReturn(describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-lg")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThatModelsAreEqual(response.getResourceModel(), filter);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_Success_fromMultipleResults() {
        final ReadHandler handler = new ReadHandler();

        final MetricFilter filter1 = MetricFilter.builder()
            .filterName("test-filter-not-it")
            .logGroupName("test-lg")
            .filterPattern("[filter pattern 1]")
            .metricTransformations(MetricTransformation.builder().metricName("metric").metricValue("value").build())
            .build();
        final MetricFilter filter2 = MetricFilter.builder()
            .filterName("test-filter")
            .logGroupName("test-lg")
            .filterPattern("[filter pattern 2]")
            .metricTransformations(MetricTransformation.builder().metricName("metric").metricValue("value").build())
            .build();
        final MetricFilter filter3 = MetricFilter.builder()
            .filterName("test-filter-not-it-either")
            .logGroupName("test-lg")
            .filterPattern("[filter pattern 3]")
            .metricTransformations(MetricTransformation.builder().metricName("metric").metricValue("value").build())
            .build();
        final DescribeMetricFiltersResponse describeResponse = DescribeMetricFiltersResponse.builder()
            .metricFilters(filter1, filter2, filter3)
            .build();

        doReturn(describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-lg")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThatModelsAreEqual(response.getResourceModel(), filter2);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_LogGroupNotFound() {
        final ReadHandler handler = new ReadHandler();

        doThrow(ResourceNotFoundException.builder().message("The specified log group does not exist.").build())
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("loggroup")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(com.amazonaws.cloudformation.exceptions.ResourceNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    @Test
    public void handleRequest_MetricFilterNotFound() {
        final ReadHandler handler = new ReadHandler();

        final DescribeMetricFiltersResponse describeResponse = DescribeMetricFiltersResponse.builder()
            .metricFilters(Collections.emptyList())
            .build();

        doReturn(describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-lg")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(com.amazonaws.cloudformation.exceptions.ResourceNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    @Test
    public void handleRequest_SimilarExistingMetricFilter_Failed() {
        final ReadHandler handler = new ReadHandler();

        // the underlying API uses a prefix match search, so the handler needs to disambiguate 'similar' describes
        final MetricFilter filter1 = MetricFilter.builder()
            .filterName("test-filter-with-longer-name")
            .logGroupName("test-lg")
            .build();
        final MetricFilter filter2 = MetricFilter.builder()
            .filterName("test-filter-with-longer-name-2")
            .logGroupName("test-lg")
            .build();
        final DescribeMetricFiltersResponse describeResponse = DescribeMetricFiltersResponse.builder()
            .metricFilters(filter1, filter2)
            .build();

        // return existing metric which share a filter name prefix, but do not match
        doReturn(describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel previous = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-lg")
            .filterPattern("some pattern")
            .build();
        final ResourceModel desired = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-lg")
            .filterPattern("some new pattern")
            .metricTransformations(Arrays.asList(
                com.aws.logs.metricfilter.MetricTransformation.builder()
                    .metricName("metric").metricValue("value").build(),
                com.aws.logs.metricfilter.MetricTransformation.builder()
                    .metricName("metric2").metricValue("value2").build()
            ))
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desired)
            .desiredResourceState(previous)
            .build();

        assertThrows(com.amazonaws.cloudformation.exceptions.ResourceNotFoundException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }
}
