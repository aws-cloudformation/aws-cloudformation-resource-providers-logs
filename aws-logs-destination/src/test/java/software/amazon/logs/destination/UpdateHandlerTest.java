package software.amazon.logs.destination;

import org.junit.jupiter.api.Assertions;
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
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    @Mock
    private CloudWatchLogsClient sdkClient;

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
    }

    @Test
    public void handleRequest_Should_ReturnSuccess_When_DestinationNotFound() {

        final Destination destination = getTestDestination();

        final DescribeDestinationsResponse describeResponse =
                DescribeDestinationsResponse.builder().destinations(destination).build();

        Mockito.when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class)))
                .thenThrow(ResourceNotFoundException.class)
                .thenReturn(describeResponse);

        final PutDestinationResponse putDestinationResponse =
                PutDestinationResponse.builder().destination(destination).build();

        final ResourceHandlerRequest<ResourceModel> request =
                ResourceHandlerRequest.<ResourceModel>builder().desiredResourceState(testResourceModel).build();

        Mockito.when(proxyClient.client().putDestination(ArgumentMatchers.any(PutDestinationRequest.class)))
                .thenReturn(putDestinationResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

//    @Test
//    public void handleRequest_Should_ReturnFailureProgressEvent_When_DestinationIsFound() {
//
//        final Destination destination = getTestDestination();
//
//        final DescribeDestinationsResponse describeResponse =
//                DescribeDestinationsResponse.builder().destinations(destination).build();
//
//        Mockito.when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class)))
//                .thenReturn(describeResponse);
//
//        final ResourceHandlerRequest<ResourceModel> request =
//                ResourceHandlerRequest.<ResourceModel>builder().desiredResourceState(testResourceModel).build();
//
//        final ProgressEvent<ResourceModel, CallbackContext> response =
//                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
//
//        assertThat(response).isNotNull();
//        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
//        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
//        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
//        assertThat(response.getResourceModels()).isNull();
//        assertThat(response.getMessage()).isNull();
//        assertThat(response.getErrorCode()).isNull();
//    }

    @Test
    public void handleRequest_Should_ThrowCfnNotFoundException_When_PutDestinationPolicyFails() {

        final Destination destination = getTestDestination();

        final DescribeDestinationsResponse describeResponse =
                DescribeDestinationsResponse.builder().destinations(destination).build();

        Mockito.when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class)))
                .thenThrow(ResourceNotFoundException.class)
                .thenReturn(describeResponse);

        final PutDestinationResponse putDestinationResponse =
                PutDestinationResponse.builder().destination(destination).build();

        final ResourceHandlerRequest<ResourceModel> request =
                ResourceHandlerRequest.<ResourceModel>builder().desiredResourceState(testResourceModel).build();

        Mockito.when(proxyClient.client().putDestination(ArgumentMatchers.any(PutDestinationRequest.class)))
                .thenReturn(putDestinationResponse);
        Mockito.when(proxyClient.client().putDestinationPolicy(ArgumentMatchers.any(PutDestinationPolicyRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        Assertions.assertThrows(CfnNotFoundException.class,
                () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    @Test
    public void handleRequest_Should_ReturnFailureProgressEvent_When_DestinationReadFails() {

        Mockito.when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class)))
                .thenThrow(InvalidParameterException.class);
        final ResourceHandlerRequest<ResourceModel> request =
                ResourceHandlerRequest.<ResourceModel>builder().desiredResourceState(testResourceModel).build();

        ProgressEvent<ResourceModel, CallbackContext> progressEvent =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        assertThat(progressEvent).isNotNull();
        assertThat(progressEvent.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(progressEvent.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }

    @Test
    public void handleRequest_Should_ReturnFailureProgressEvent_When_DestinationIsNotUpdatable() {

        ResourceModel previousResourceModel = ResourceModel.builder()
                .destinationName("DestinationName2")
                .roleArn(TEST_ROLE_ARN)
                .targetArn(TEST_TARGET_ARN)
                .destinationPolicy(TEST_DESTINATION_INPUT)
                .build();
        final ResourceHandlerRequest<ResourceModel> request =
                ResourceHandlerRequest.<ResourceModel>builder().desiredResourceState(testResourceModel)
                        .previousResourceState(previousResourceModel)
                        .build();

        ProgressEvent<ResourceModel, CallbackContext> progressEvent =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        assertThat(progressEvent).isNotNull();
        assertThat(progressEvent.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(progressEvent.getErrorCode()).isEqualTo(HandlerErrorCode.NotUpdatable);
    }

}
