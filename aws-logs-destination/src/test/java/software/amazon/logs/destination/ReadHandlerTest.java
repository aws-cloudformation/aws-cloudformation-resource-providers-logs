package software.amazon.logs.destination;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.Destination;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;

@ExtendWith(MockitoExtension.class)
class ReadHandlerTest extends AbstractTestBase {

    @Mock
    private CloudWatchLogsClient sdkClient;

    @Mock
    private MetricsLogger metrics;

    private AmazonWebServicesClientProxy proxy;

    private ProxyClient<CloudWatchLogsClient> proxyClient;

    private ResourceModel testResourceModel;

    private ReadHandler handler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        testResourceModel = getTestResourceModel();
        handler = new ReadHandler();
        metrics = mock(MetricsLogger.class);
    }

    @Test
    void handleRequest_Should_ReturnSuccess_When_DestinationFound() {
        final Destination destination = getTestDestination();

        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse.builder().destinations(destination).build();

        Mockito
            .when(proxyClient.client().describeDestinations(ArgumentMatchers.any(DescribeDestinationsRequest.class)))
            .thenReturn(describeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(testResourceModel)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
            proxy,
            request,
            new CallbackContext(),
            proxyClient,
            logger,
            metrics
        );

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        Assertions.assertThat(response.getCallbackDelaySeconds()).isZero();
        Assertions.assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        Assertions.assertThat(response.getResourceModels()).isNull();
        Assertions.assertThat(response.getMessage()).isNull();
        Assertions.assertThat(response.getErrorCode()).isNull();
    }

    @Test
    void handleRequest_Should_ThrowCfnNotFoundException_When_DestinationsIsEmpty() {
        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse
            .builder()
            .destinations(Collections.emptyList())
            .build();

        Mockito
            .when(proxyClient.client().describeDestinations(ArgumentMatchers.any(DescribeDestinationsRequest.class)))
            .thenReturn(describeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(testResourceModel)
            .build();

        Assertions
            .assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics))
            .isInstanceOf(CfnNotFoundException.class);
    }

    @Test
    void handleRequest_Should_ThrowCfnNotFoundException_When_ResponseIsNull() {
        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse.builder().build();
        Mockito
            .when(proxyClient.client().describeDestinations(ArgumentMatchers.any(DescribeDestinationsRequest.class)))
            .thenReturn(describeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(testResourceModel)
            .build();

        Assertions
            .assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics))
            .isInstanceOf(CfnNotFoundException.class);
    }

    @Test
    void handleRequest_Should_ThrowCfnInvalidRequestException__When_InvalidDestinationIsPassed() {
        Mockito
            .when(proxyClient.client().describeDestinations(ArgumentMatchers.any(DescribeDestinationsRequest.class)))
            .thenThrow(InvalidParameterException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(testResourceModel)
            .build();
        assertThrows(
            CfnInvalidRequestException.class,
            () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics)
        );
    }
}
