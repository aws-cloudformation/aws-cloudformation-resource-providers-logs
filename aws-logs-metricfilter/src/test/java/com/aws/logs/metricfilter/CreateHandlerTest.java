package com.aws.logs.metricfilter;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.HandlerErrorCode;
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
import software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {
    private static final String PRIMARY_ID = "{\"LogGroupName\":[\"test-log-group\"],\"FilterName\":[\"test-filter\"]}";

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
        final CreateHandler handler = new CreateHandler();

        final DescribeMetricFiltersResponse describeResponse = DescribeMetricFiltersResponse.builder()
            .metricFilters(Collections.emptyList())
            .build();

        final PutMetricDataResponse createResponse = PutMetricDataResponse.builder().build();

        // return no existing metrics for pre-create and then success response for create
        doReturn(describeResponse)
            .doReturn(createResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-log-group")
            .filterPattern("some pattern")
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
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_FailedCreate_UnknownError() {
        final CreateHandler handler = new CreateHandler();

        // throw arbitrary error which should propagate to be handled by wrapper
        doThrow(SdkException.builder().message("test error").build())
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-log-group")
            .filterPattern("some pattern")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(SdkException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    @Test
    public void handleRequest_FailedCreate_AmazonServiceException() {
        final CreateHandler handler = new CreateHandler();

        // AmazonServiceExceptions should be thrown so they can be handled by wrapper
        doThrow(new AmazonServiceException("test error"))
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .filterName("test-filter")
            .logGroupName("test-log-group")
            .filterPattern("some pattern")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(AmazonServiceException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    @Test
    public void handleRequest_FailedPreExisting() {
        final CreateHandler handler = new CreateHandler();

        final MetricFilter filter = MetricFilter.builder()
            .filterName("test-filter")
            .logGroupName("test-log-group")
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
            .logGroupName("test-log-group")
            .filterPattern("some pattern")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getMessage()).isEqualTo(Translator.buildResourceAlreadyExistsErrorMessage(PRIMARY_ID));
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.AlreadyExists);
    }

    @Test
    public void handleRequest_WithGeneratedName() {
        final CreateHandler handler = new CreateHandler();

        final DescribeMetricFiltersResponse describeResponse = DescribeMetricFiltersResponse.builder()
            .metricFilters(Collections.emptyList())
            .build();

        final PutMetricDataResponse createResponse = PutMetricDataResponse.builder().build();

        // return no existing metrics for pre-create and then success response for create
        doReturn(describeResponse)
            .doReturn(createResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        // no name supplied; should be generated
        final ResourceModel model = ResourceModel.builder()
            .logGroupName("test-log-group")
            .filterPattern("some pattern")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .clientRequestToken(UUID.randomUUID().toString())
            .logicalResourceIdentifier("myMetricFilter")
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModels()).<ResourceModel>isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        ResourceModel outModel = response.getResourceModel();
        assertThat(outModel.getFilterName()).startsWith("myMetricFilter");
    }
}
