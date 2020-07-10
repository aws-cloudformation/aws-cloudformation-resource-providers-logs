package software.amazon.logs.metricfilter;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<CloudWatchLogsClient> proxyClient;

    @Mock
    CloudWatchLogsClient sdkClient;

    final CreateHandler handler = new CreateHandler();

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(CloudWatchLogsClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @AfterEach
    public void tear_down() {
        verify(sdkClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(sdkClient);
    }

    @Test
    public void handleRequest_Success() {
        final ResourceModel model = buildDefaultModel();

        final DescribeMetricFiltersResponse describeResponse = DescribeMetricFiltersResponse.builder()
                .metricFilters(Translator.translateToSDK(model))
                .build();

        final PutMetricFilterResponse createResponse = PutMetricFilterResponse.builder()
                .build();

        // return no existing metrics for pre-create and then success response for create
        when(proxyClient.client().describeMetricFilters(any(DescribeMetricFiltersRequest.class)))
                .thenThrow(ResourceNotFoundException.class)
                .thenReturn(describeResponse);

        when(proxyClient.client().putMetricFilter(any(PutMetricFilterRequest.class)))
                .thenReturn(createResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        verify(proxyClient.client()).describeMetricFilters(any(DescribeMetricFiltersRequest.class));
        verify(proxyClient.client()).putMetricFilter(any(PutMetricFilterRequest.class));
    }

    @Test
    public void handleRequest_Success2() {
        final ResourceModel model = buildDefaultModel();

        final DescribeMetricFiltersResponse preCreateResponse = DescribeMetricFiltersResponse.builder()
                .metricFilters(Collections.emptyList())
                .build();

        final PutMetricFilterResponse createResponse = PutMetricFilterResponse.builder()
                .build();

        // return no existing metrics for pre-create and then success response for create
        when(proxyClient.client().describeMetricFilters(any(DescribeMetricFiltersRequest.class)))
                .thenReturn(preCreateResponse);

        when(proxyClient.client().putMetricFilter(any(PutMetricFilterRequest.class)))
                .thenReturn(createResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        verify(proxyClient.client()).describeMetricFilters(any(DescribeMetricFiltersRequest.class));
        verify(proxyClient.client()).putMetricFilter(any(PutMetricFilterRequest.class));
    }

    @Test
    public void handleRequest_FailedCreate_InternalReadThrowsException() {
        final ResourceModel model = buildDefaultModel();

        // throw arbitrary error which should propagate to be handled by wrapper
        when(proxyClient.client().describeMetricFilters(any(DescribeMetricFiltersRequest.class)))
                .thenThrow(ServiceUnavailableException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();


        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnServiceInternalErrorException.class);
    }

    @Test
    public void handleRequest_FailedCreate_AlreadyExists() {
        final ResourceModel model = buildDefaultModel();

        final DescribeMetricFiltersResponse describeResponse = DescribeMetricFiltersResponse.builder()
                .metricFilters(Translator.translateToSDK(model))
                .build();

        when(proxyClient.client().describeMetricFilters(any(DescribeMetricFiltersRequest.class)))
                .thenReturn(describeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnAlreadyExistsException.class);
    }

    @Test
    public void handleRequest_FailedCreate_PutFailed() {
        final ResourceModel model = buildDefaultModel();

        final DescribeMetricFiltersResponse describeResponse = DescribeMetricFiltersResponse.builder()
                .metricFilters(Translator.translateToSDK(model))
                .build();

        // return no existing metrics for pre-create and then success response for create
        when(proxyClient.client().describeMetricFilters(any(DescribeMetricFiltersRequest.class)))
                .thenThrow(ResourceNotFoundException.class)
                .thenReturn(describeResponse);

        when(proxyClient.client().putMetricFilter(any(PutMetricFilterRequest.class)))
                .thenThrow(OperationAbortedException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnResourceConflictException.class);
    }

    @Test
    public void handleRequest_Success_WithGeneratedName() {
        // no filter name supplied; should be generated
        final ResourceModel model = ResourceModel.builder()
                .logGroupName("test-log-group")
                .filterPattern("some pattern")
                .metricTransformations(Arrays.asList(MetricTransformation.builder()
                        .metricName("metric-name")
                        .metricValue("0")
                        .metricNamespace("namespace")
                        .build()))
                .build();

        // return no existing metrics for pre-create and then success response for create
        when(proxyClient.client().describeMetricFilters(any(DescribeMetricFiltersRequest.class)))
                .thenThrow(ResourceNotFoundException.class)
                .thenReturn(DescribeMetricFiltersResponse.builder()
                        .metricFilters(Translator.translateToSDK(model))
                        .build());

        when(proxyClient.client().putMetricFilter(any(PutMetricFilterRequest.class)))
                .thenReturn(PutMetricFilterResponse.builder().build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .logicalResourceIdentifier("logicalResourceIdentifier")
                .clientRequestToken("requestToken")
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
