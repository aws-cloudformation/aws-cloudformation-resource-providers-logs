package software.amazon.logs.subscriptionfilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchevents.model.InternalException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutSubscriptionFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutSubscriptionFilterResponse;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;

@ExtendWith(MockitoExtension.class)
class UpdateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<CloudWatchLogsClient> proxyClient;

    @Mock
    CloudWatchLogsClient sdkClient;

    @Mock
    MetricsLogger metrics;

    final UpdateHandler handler = new UpdateHandler();

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(CloudWatchLogsClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        metrics = mock(MetricsLogger.class);
    }

    @Test
    void handleRequest_Success() {
        final PutSubscriptionFilterResponse updateResponse = PutSubscriptionFilterResponse.builder().build();

        final ResourceModel model = buildDefaultModel();

        final DescribeSubscriptionFiltersResponse describeResponse = DescribeSubscriptionFiltersResponse
            .builder()
            .subscriptionFilters(Translator.translateToSDK(model))
            .build();

        when(proxyClient.client().putSubscriptionFilter(ArgumentMatchers.any(PutSubscriptionFilterRequest.class)))
            .thenReturn(updateResponse);
        when(proxyClient.client().describeSubscriptionFilters(ArgumentMatchers.any(DescribeSubscriptionFiltersRequest.class)))
            .thenReturn(describeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
            proxy,
            request,
            new CallbackContext(),
            proxyClient,
            logger,
            metrics
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        verify(proxyClient.client()).describeSubscriptionFilters(any(DescribeSubscriptionFiltersRequest.class));
        verify(proxyClient.client()).putSubscriptionFilter(any(PutSubscriptionFilterRequest.class));
        verify(sdkClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(sdkClient);
    }

    @Test
    void handleRequest_ResourceNotFound() {
        final ResourceModel model = buildDefaultModel();

        final DescribeSubscriptionFiltersResponse describeResponse = DescribeSubscriptionFiltersResponse
            .builder()
            .subscriptionFilters(Collections.emptyList())
            .build();

        when(proxyClient.client().describeSubscriptionFilters(ArgumentMatchers.any(DescribeSubscriptionFiltersRequest.class)))
            .thenReturn(describeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics))
            .isInstanceOf(CfnNotFoundException.class);
    }

    @Test
    void handleRequest_InternalException() {
        final ResourceModel model = buildDefaultModel();

        final DescribeSubscriptionFiltersResponse describeResponse = DescribeSubscriptionFiltersResponse
            .builder()
            .subscriptionFilters(Translator.translateToSDK(model))
            .build();

        when(proxyClient.client().describeSubscriptionFilters(ArgumentMatchers.any(DescribeSubscriptionFiltersRequest.class)))
            .thenReturn(describeResponse);

        when(proxyClient.client().putSubscriptionFilter(ArgumentMatchers.any(PutSubscriptionFilterRequest.class)))
            .thenThrow(InternalException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
            proxy,
            request,
            new CallbackContext(),
            proxyClient,
            logger,
            metrics
        );
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModels()).isNull();

        verify(proxyClient.client(), times(1)).describeSubscriptionFilters(any(DescribeSubscriptionFiltersRequest.class));
        verify(proxyClient.client(), times(1)).putSubscriptionFilter(any(PutSubscriptionFilterRequest.class));
        verify(sdkClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(sdkClient);
    }
}
