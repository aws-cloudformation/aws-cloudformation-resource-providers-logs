package software.amazon.logs.metricfilter;

import java.time.Duration;
import java.util.Collections;

import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<CloudWatchLogsClient> proxyClient;

    @Mock
    CloudWatchLogsClient sdkClient;

    final ReadHandler handler = new ReadHandler();

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(CloudWatchLogsClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void handleRequest_Success() {
        final ResourceModel model = buildDefaultModel();

        final DescribeMetricFiltersResponse describeResponse = DescribeMetricFiltersResponse.builder()
                .metricFilters(Translator.translateToSDK(model))
                .build();

        when(proxyClient.client().describeMetricFilters(ArgumentMatchers.any(DescribeMetricFiltersRequest.class)))
                .thenReturn(describeResponse);

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
        verify(proxyClient.client(), times(1)).describeMetricFilters(any(DescribeMetricFiltersRequest.class));
    }

    @Test
    public void handleRequest_ResponseIsEmpty() {
        final ResourceModel model = buildDefaultModel();

        final DescribeMetricFiltersResponse describeResponse = DescribeMetricFiltersResponse.builder()
                .metricFilters(Collections.emptyList())
                .build();

        when(proxyClient.client().describeMetricFilters(ArgumentMatchers.any(DescribeMetricFiltersRequest.class)))
                .thenReturn(describeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnNotFoundException.class);
    }

    @Test
    public void handleRequest_ResourceNotFound() {
        final ResourceModel model = buildDefaultModel();

        when(proxyClient.client().describeMetricFilters(ArgumentMatchers.any(DescribeMetricFiltersRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnNotFoundException.class);
    }

    @Test
    public void handleRequest_ExceptionThrown() {
        final ResourceModel model = buildDefaultModel();

        when(proxyClient.client().describeMetricFilters(ArgumentMatchers.any(DescribeMetricFiltersRequest.class)))
                .thenThrow(InvalidParameterException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnInvalidRequestException.class);
    }
}
