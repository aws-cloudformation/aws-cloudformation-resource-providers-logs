package software.amazon.logs.destination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.Destination;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;

@ExtendWith(MockitoExtension.class)
class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private CloudWatchLogsClient sdkClient;

    @Mock
    private MetricsLogger metrics;

    private AmazonWebServicesClientProxy proxy;

    private ProxyClient<CloudWatchLogsClient> proxyClient;

    private ResourceModel testResourceModel;

    private Destination destination;

    private Destination destinationWithoutPolicy;

    private CreateHandler handler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        testResourceModel = getTestResourceModel();
        destination = getTestDestination();
        destinationWithoutPolicy = getTestDestination(false);
        handler = new CreateHandler();
        metrics = mock(MetricsLogger.class);
    }

    @Test
    void handleRequest_Should_ReturnSuccess_When_DestinationNotFound_Then_Created() {
        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse.builder().destinations(destination).build();

        when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class)))
            .thenThrow(ResourceNotFoundException.class)
            .thenReturn(describeResponse);

        final PutDestinationResponse putDestinationResponse = PutDestinationResponse.builder().destination(destination).build();

        final ResourceHandlerRequest<ResourceModel> request = getDefaultRequestBuilder().desiredResourceState(testResourceModel).build();

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
    void handleRequest_Should_ReturnSuccess_When_DescribeDestinationsResponseIsNull() {
        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse.builder().destinations(destination).build();

        when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class)))
            .thenReturn(DescribeDestinationsResponse.builder().build())
            .thenReturn(describeResponse);

        final PutDestinationResponse putDestinationResponse = PutDestinationResponse.builder().destination(destination).build();

        final ResourceHandlerRequest<ResourceModel> request = getDefaultRequestBuilder().desiredResourceState(testResourceModel).build();

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
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);

        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    void handleRequest_Should_ReturnSuccess_When_DescribeDestinationsResponseIsEmpty() {
        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse.builder().destinations(destination).build();

        when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class)))
            .thenReturn(DescribeDestinationsResponse.builder().destinations(Collections.emptyList()).build())
            .thenReturn(describeResponse);

        final PutDestinationResponse putDestinationResponse = PutDestinationResponse.builder().destination(destination).build();

        final ResourceHandlerRequest<ResourceModel> request = getDefaultRequestBuilder().desiredResourceState(testResourceModel).build();

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

    private ResourceHandlerRequest.ResourceHandlerRequestBuilder<ResourceModel> getDefaultRequestBuilder() {
        return ResourceHandlerRequest
            .<ResourceModel>builder()
            .logicalResourceIdentifier("logicalResourceIdentifier")
            .clientRequestToken("requestToken");
    }

    @Test
    void handleRequest_Should_ReturnFailureProgressEvent_When_DestinationIsFound() {
        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse.builder().destinations(destination).build();

        when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class))).thenReturn(describeResponse);

        final ResourceHandlerRequest<ResourceModel> request = getDefaultRequestBuilder().desiredResourceState(testResourceModel).build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics))
            .isInstanceOf(CfnAlreadyExistsException.class)
            .hasMessageContaining(destination.destinationName());
    }

    @Test
    void handleRequest_Should_ThrowCfnResourceConflictException_When_PutOperationIsAborted() {
        when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class)))
            .thenThrow(ResourceNotFoundException.class)
            .thenReturn(DescribeDestinationsResponse.builder().destinations(destination).build());

        when(proxyClient.client().putDestination(ArgumentMatchers.any(PutDestinationRequest.class)))
            .thenThrow(OperationAbortedException.class);

        final ResourceHandlerRequest<ResourceModel> request = getDefaultRequestBuilder().desiredResourceState(testResourceModel).build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics))
            .isInstanceOf(CfnResourceConflictException.class);
    }

    @Test
    void handleRequest_Should_ThrowCfnNotFoundException_When_PutDestinationPolicyFails() {
        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse.builder().destinations(destination).build();

        when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class)))
            .thenThrow(ResourceNotFoundException.class)
            .thenReturn(describeResponse);

        final PutDestinationResponse putDestinationResponse = PutDestinationResponse.builder().destination(destination).build();

        final ResourceHandlerRequest<ResourceModel> request = getDefaultRequestBuilder().desiredResourceState(testResourceModel).build();

        when(proxyClient.client().putDestination(ArgumentMatchers.any(PutDestinationRequest.class))).thenReturn(putDestinationResponse);
        when(proxyClient.client().putDestinationPolicy(ArgumentMatchers.any(PutDestinationPolicyRequest.class)))
            .thenThrow(ResourceNotFoundException.class);

        assertThrows(
            CfnNotFoundException.class,
            () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics)
        );
    }

    @Test
    void handleRequest_Should_ReturnFailureProgressEvent_When_DestinationReadFails() {
        when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class))).thenThrow(InvalidParameterException.class);
        final ResourceHandlerRequest<ResourceModel> request = getDefaultRequestBuilder().desiredResourceState(testResourceModel).build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics))
            .isInstanceOf(CfnInvalidRequestException.class);
    }

    @Test
    void handleRequest_Should_ReturnFailureProgressEvent_When_DestinationReadFailsWithCloudWatchLogException() {
        when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class))).thenThrow(CloudWatchLogsException.class);
        final ResourceHandlerRequest<ResourceModel> request = getDefaultRequestBuilder().desiredResourceState(testResourceModel).build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics))
            .isInstanceOf(CfnGeneralServiceException.class);
    }

    // tests for optional parameter, destination policy not provided tests
    @Test
    void handleRequest_Should_ReturnSuccess_When_DestinationNotFound_and_DestinationPolicyNotProvided() {
        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse
            .builder()
            .destinations(destinationWithoutPolicy)
            .build();
        when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class)))
            .thenThrow(ResourceNotFoundException.class)
            .thenReturn(describeResponse);

        final PutDestinationResponse putDestinationResponse = PutDestinationResponse
            .builder()
            .destination(destinationWithoutPolicy)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = getDefaultRequestBuilder().desiredResourceState(testResourceModel).build();

        //set destination policy to "" to mimick not being provided
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

        //following line causes error, response doesn't include value for dest.policy it seems, but desiredResourceState does include it
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    void handleRequest_Should_ReturnSuccess_When_DescribeDestinationsResponseIsNull_and_DestinationPolicyNotProvided() {
        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse
            .builder()
            .destinations(destinationWithoutPolicy)
            .build();

        when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class)))
            .thenReturn(DescribeDestinationsResponse.builder().build())
            .thenReturn(describeResponse);

        final PutDestinationResponse putDestinationResponse = PutDestinationResponse
            .builder()
            .destination(destinationWithoutPolicy)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = getDefaultRequestBuilder().desiredResourceState(testResourceModel).build();

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
    void handleRequest_Should_ReturnSuccess_When_DescribeDestinationsResponseIsEmpty_and_DestinationPolicyNotProvided() {
        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse
            .builder()
            .destinations(destinationWithoutPolicy)
            .build();

        when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class)))
            .thenReturn(DescribeDestinationsResponse.builder().destinations(Collections.emptyList()).build())
            .thenReturn(describeResponse);

        final PutDestinationResponse putDestinationResponse = PutDestinationResponse
            .builder()
            .destination(destinationWithoutPolicy)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = getDefaultRequestBuilder().desiredResourceState(testResourceModel).build();

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
}
