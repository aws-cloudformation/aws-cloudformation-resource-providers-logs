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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.LimitExceededException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;

@ExtendWith(MockitoExtension.class)
class ReadHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<CloudWatchLogsClient> proxyClient;

    @Mock
    CloudWatchLogsClient sdkClient;

    @Mock
    MetricsLogger metrics;

    final ReadHandler handler = new ReadHandler();

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(CloudWatchLogsClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        metrics = mock(MetricsLogger.class);
    }

    @AfterEach
    public void tear_down() {
        verify(sdkClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(sdkClient);
    }

    @Test
    void handleRequest_Success() {
        final ResourceModel model = buildDefaultModel();
        final DescribeSubscriptionFiltersResponse describeResponse = DescribeSubscriptionFiltersResponse
            .builder()
            .subscriptionFilters(Translator.translateToSDK(model))
            .build();

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
        verify(proxyClient.client(), times(1)).describeSubscriptionFilters(any(DescribeSubscriptionFiltersRequest.class));
    }

    @Test
    void handleRequest_ResponseIsEmpty() {
        final ResourceModel model = buildDefaultModel();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final DescribeSubscriptionFiltersResponse describeResponse = DescribeSubscriptionFiltersResponse
            .builder()
            .subscriptionFilters(Collections.emptyList())
            .build();

        when(proxyClient.client().describeSubscriptionFilters(ArgumentMatchers.any(DescribeSubscriptionFiltersRequest.class)))
            .thenReturn(describeResponse);

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics))
            .isInstanceOf(CfnNotFoundException.class);
    }

    @Test
    void handleRequest_ServiceLimitExceptionThrown() {
        final ResourceModel model = buildDefaultModel();

        when(proxyClient.client().describeSubscriptionFilters(ArgumentMatchers.any(DescribeSubscriptionFiltersRequest.class)))
            .thenThrow(LimitExceededException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics))
            .isInstanceOf(CfnServiceLimitExceededException.class);
    }
}
