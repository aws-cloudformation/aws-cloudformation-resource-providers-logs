package software.amazon.logs.destination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.Destination;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
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
    private CloudWatchLogsClient sdkClient;

    @Mock
    private MetricsLogger metrics;

    private AmazonWebServicesClientProxy proxy;

    private ProxyClient<CloudWatchLogsClient> proxyClient;

    private ResourceModel testResourceModel;

    private UpdateHandler handler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        testResourceModel = getTestResourceModel();
        handler = new UpdateHandler();
        metrics = mock(MetricsLogger.class);
    }

    @Test
    void handleRequest_Should_ReturnFailure_When_DescribeDestinationsResponseIsNull() {
        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse.builder().build();

        when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class))).thenReturn(describeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(testResourceModel)
            .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics))
            .isInstanceOf(CfnNotFoundException.class);
    }

    @Test
    void handleRequest_Should_ReturnFailure_When_DescribeDestinationsResponseIsEmpty() {
        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse
            .builder()
            .destinations(Collections.emptyList())
            .build();

        when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class))).thenReturn(describeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(testResourceModel)
            .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics))
            .isInstanceOf(CfnNotFoundException.class);
    }

    @Test
    void handleRequest_Should_ReturnFailure_When_DestinationReadFails() {
        when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class))).thenThrow(InvalidParameterException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(testResourceModel)
            .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics))
            .isInstanceOf(CfnInvalidRequestException.class);
    }

    @Test
    void handleRequest_Should_ReturnSuccess_Given_DestinationWithoutPolicy_When_UpdateDestination_With_NewPolicy() {
        final Destination destinationWithoutPolicy = getTestDestination(false);

        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse
            .builder()
            .destinations(destinationWithoutPolicy)
            .build();

        when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class))).thenReturn(describeResponse);

        final PutDestinationResponse putDestinationResponse = PutDestinationResponse
            .builder()
            .destination(destinationWithoutPolicy)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(testResourceModel)
            .build();

        request.getDesiredResourceState().setDestinationPolicy(null);

        when(proxyClient.client().putDestination(ArgumentMatchers.any(PutDestinationRequest.class))).thenReturn(putDestinationResponse);

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
    }

    @Test
    void handleRequest_Should_ReturnSuccess_Given_DestinationWithPolicy_When_UpdateDestination_With_NewPolicy() {
        final Destination destinationWithPolicy = getTestDestination(true);

        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse
            .builder()
            .destinations(destinationWithPolicy)
            .build();

        when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class))).thenReturn(describeResponse);

        final PutDestinationResponse putDestinationResponse = PutDestinationResponse.builder().destination(destinationWithPolicy).build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(testResourceModel)
            .build();

        when(proxyClient.client().putDestination(ArgumentMatchers.any(PutDestinationRequest.class))).thenReturn(putDestinationResponse);

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
    }
}
