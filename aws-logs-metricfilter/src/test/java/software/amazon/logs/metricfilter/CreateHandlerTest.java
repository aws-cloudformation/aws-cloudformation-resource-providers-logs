package software.amazon.logs.metricfilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;

@ExtendWith(MockitoExtension.class)
class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<CloudWatchLogsClient> proxyClient;

    @Mock
    CloudWatchLogsClient sdkClient;

    final CreateHandler handler = new CreateHandler();

    @Mock
    private MetricsLogger metrics;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(CloudWatchLogsClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        metrics = mock(MetricsLogger.class);
    }

    @AfterEach
    public void tear_down() {
        verifyNoMoreInteractions(sdkClient);
    }

    @Test
    void handleRequest_Success() {
        // by default the model contains dimensions and units, pass in false to the function below to remove dimensions/units, see AbstractTestBase for more details
        final ResourceModel model = buildDefaultModel();

        final DescribeMetricFiltersResponse describeResponse = DescribeMetricFiltersResponse
            .builder()
            .metricFilters(Translator.translateToSDK(model))
            .build();

        final PutMetricFilterResponse createResponse = PutMetricFilterResponse.builder().build();

        // return no existing metrics for pre-create and then success response for create
        when(proxyClient.client().describeMetricFilters(any(DescribeMetricFiltersRequest.class)))
            .thenThrow(ResourceNotFoundException.class)
            .thenReturn(describeResponse);

        when(proxyClient.client().putMetricFilter(any(PutMetricFilterRequest.class))).thenReturn(createResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        CallbackContext callbackContext = new CallbackContext();

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
            proxy,
            request,
            callbackContext,
            proxyClient,
            logger,
            metrics
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isPositive();

        response = handler.handleRequest(proxy, request, callbackContext, proxyClient, logger, metrics);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).describeMetricFilters(any(DescribeMetricFiltersRequest.class));
        verify(proxyClient.client()).putMetricFilter(any(PutMetricFilterRequest.class));
        verify(sdkClient, atLeastOnce()).serviceName();
    }

    @Test
    void handleRequest_Success2() {
        final ResourceModel model = buildDefaultModel();

        final DescribeMetricFiltersResponse preCreateResponse = DescribeMetricFiltersResponse
            .builder()
            .metricFilters(Collections.emptyList())
            .build();

        final DescribeMetricFiltersResponse describeResponse = DescribeMetricFiltersResponse
            .builder()
            .metricFilters(Translator.translateToSDK(model))
            .build();

        final PutMetricFilterResponse createResponse = PutMetricFilterResponse.builder().build();

        // return no existing metrics for pre-create and then success response for create
        when(proxyClient.client().describeMetricFilters(any(DescribeMetricFiltersRequest.class)))
            .thenReturn(preCreateResponse)
            .thenReturn(describeResponse);

        when(proxyClient.client().putMetricFilter(any(PutMetricFilterRequest.class))).thenReturn(createResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        CallbackContext callbackContext = new CallbackContext();

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
            proxy,
            request,
            callbackContext,
            proxyClient,
            logger,
            metrics
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isPositive();

        response = handler.handleRequest(proxy, request, callbackContext, proxyClient, logger, metrics);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).describeMetricFilters(any(DescribeMetricFiltersRequest.class));
        verify(proxyClient.client()).putMetricFilter(any(PutMetricFilterRequest.class));
        verify(sdkClient, atLeastOnce()).serviceName();
    }

    @Test
    void handleRequest_FailedCreate_InternalReadThrowsException() {
        final ResourceModel model = buildDefaultModel();

        // throw arbitrary error which should propagate to be handled by wrapper
        when(proxyClient.client().describeMetricFilters(any(DescribeMetricFiltersRequest.class)))
            .thenThrow(ServiceUnavailableException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics))
            .isInstanceOf(CfnServiceInternalErrorException.class);
    }

    @Test
    void handleRequest_FailedCreate_AlreadyExists() {
        final ResourceModel model = buildDefaultModel();

        final DescribeMetricFiltersResponse describeResponse = DescribeMetricFiltersResponse
            .builder()
            .metricFilters(Translator.translateToSDK(model))
            .build();

        when(proxyClient.client().describeMetricFilters(any(DescribeMetricFiltersRequest.class))).thenReturn(describeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
            proxy,
            request,
            new CallbackContext(),
            proxyClient,
            logger,
            metrics
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getMessage())
            .isEqualTo(
                "Metric Filter with name " + model.getFilterName() + " already exists in log group " + model.getLogGroupName() + "."
            );
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.AlreadyExists);
        assertThat(response.getCallbackDelaySeconds()).isZero();
    }

    @Test
    void handleRequest_FailedCreate_PutFailed() {
        final ResourceModel model = buildDefaultModel();

        // return no existing metrics for pre-create and then success response for create
        when(proxyClient.client().describeMetricFilters(any(DescribeMetricFiltersRequest.class)))
            .thenThrow(ResourceNotFoundException.class);

        when(proxyClient.client().putMetricFilter(any(PutMetricFilterRequest.class))).thenThrow(OperationAbortedException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        CallbackContext callbackContext = new CallbackContext();

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
            proxy,
            request,
            callbackContext,
            proxyClient,
            logger,
            metrics
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isPositive();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, callbackContext, proxyClient, logger, metrics))
            .isInstanceOf(CfnResourceConflictException.class);

        verify(sdkClient, atLeastOnce()).serviceName();
    }

    @Test
    void handleRequest_Success_WithGeneratedName() {
        // no filter name supplied; should be generated
        final ResourceModel model = ResourceModel
            .builder()
            .logGroupName("test-log-group")
            .filterPattern("some pattern")
            .metricTransformations(
                Arrays.asList(
                    MetricTransformation.builder().metricName("metric-name").metricValue("0").metricNamespace("namespace").build()
                )
            )
            .build();

        // return no existing metrics for pre-create and then success response for create
        when(proxyClient.client().describeMetricFilters(any(DescribeMetricFiltersRequest.class)))
            .thenThrow(ResourceNotFoundException.class)
            .thenReturn(DescribeMetricFiltersResponse.builder().metricFilters(Translator.translateToSDK(model)).build());

        when(proxyClient.client().putMetricFilter(any(PutMetricFilterRequest.class))).thenReturn(PutMetricFilterResponse.builder().build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(model)
            .logicalResourceIdentifier("logicalResourceIdentifier")
            .clientRequestToken("requestToken")
            .build();

        CallbackContext callbackContext = new CallbackContext();

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
            proxy,
            request,
            callbackContext,
            proxyClient,
            logger,
            metrics
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isPositive();

        response = handler.handleRequest(proxy, request, callbackContext, proxyClient, logger, metrics);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(sdkClient, atLeastOnce()).serviceName();
    }

    @Test
    void handleRequest_idempotentCall_previousLostCallSucceeded_shouldSucceed() {
        final ResourceModel model = buildDefaultModel();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        when(proxyClient.client().putMetricFilter(any(PutMetricFilterRequest.class))).thenReturn(PutMetricFilterResponse.builder().build());

        CallbackContext callbackContext = new CallbackContext();

        // Simulates the first invocation having done the pre-check but not having returned
        callbackContext.setPreCreateCheckDone(true);

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
            proxy,
            request,
            callbackContext,
            proxyClient,
            logger,
            metrics
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());

        verify(sdkClient, atLeastOnce()).serviceName();
    }
}
