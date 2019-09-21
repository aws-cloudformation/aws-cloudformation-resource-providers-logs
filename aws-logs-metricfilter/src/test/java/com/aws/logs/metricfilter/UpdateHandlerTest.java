package com.aws.logs.metricfilter;

import com.amazonaws.AmazonServiceException;
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
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.MetricFilter;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

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
        final UpdateHandler handler = new UpdateHandler();

        final MetricFilter filter = MetricFilter.builder()
            .filterName("test-filter")
            .logGroupName("test-log-group")
            .filterPattern("[test]")
            .metricTransformations(software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation.builder().metricName("metric").metricValue("value").build())
            .build();
        final DescribeMetricFiltersResponse describeResponse = DescribeMetricFiltersResponse.builder()
            .metricFilters(filter)
            .build();

        final PutMetricDataResponse createResponse = PutMetricDataResponse.builder().build();

        // return existing metrics for pre-update check and then success response
        doReturn(describeResponse)
            .doReturn(createResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel previous = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-log-group")
            .filterPattern("some pattern")
            .build();
        final ResourceModel desired = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-log-group")
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
            .previousResourceState(previous)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel()).isEqualTo(desired);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_FailedCreate_UnknownError() {
        final UpdateHandler handler = new UpdateHandler();

        // throw arbitrary error which should propagate to be handled by wrapper
        doThrow(SdkException.builder().message("test error").build())
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel previous = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-log-group")
            .filterPattern("some pattern")
            .build();
        final ResourceModel desired = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-log-group")
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

        assertThrows(SdkException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    @Test
    public void handleRequest_FailedCreate_AmazonServiceException() {
        final UpdateHandler handler = new UpdateHandler();

        // AmazonServiceExceptions should be thrown so they can be handled by wrapper
        doThrow(new AmazonServiceException("test error"))
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel previous = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-log-group")
            .filterPattern("some pattern")
            .build();
        final ResourceModel desired = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-log-group")
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

        assertThrows(AmazonServiceException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    @Test
    public void handleRequest_NonExistingLogGroup_Failed() {
        final UpdateHandler handler = new UpdateHandler();

        // no matching log group throws exception
        doThrow(ResourceNotFoundException.class)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel previous = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-log-group")
            .filterPattern("some pattern")
            .build();
        final ResourceModel desired = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-log-group")
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

        assertThrows(com.amazonaws.cloudformation.exceptions.ResourceNotFoundException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_NonExistingMetricFilter_Failed() {
        final UpdateHandler handler = new UpdateHandler();

        final DescribeMetricFiltersResponse describeResponse = software.amazon.awssdk.services.cloudwatchlogs.model
            .DescribeMetricFiltersResponse.builder()
            .metricFilters(Collections.emptyList())
            .build();

        // no matching metric filter returns as empty list
        doReturn(describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel previous = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-log-group")
            .filterPattern("some pattern")
            .build();
        final ResourceModel desired = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-log-group")
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

        assertThrows(com.amazonaws.cloudformation.exceptions.ResourceNotFoundException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }
}
