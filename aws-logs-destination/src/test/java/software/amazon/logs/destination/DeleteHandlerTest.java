package software.amazon.logs.destination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteDestinationRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteDestinationResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;

@ExtendWith(MockitoExtension.class)
class DeleteHandlerTest extends AbstractTestBase {

    @Mock
    CloudWatchLogsClient sdkClient;

    @Mock
    private MetricsLogger metrics;

    private AmazonWebServicesClientProxy proxy;

    private ProxyClient<CloudWatchLogsClient> proxyClient;

    private ResourceModel model;

    private DeleteHandler handler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        model = getTestResourceModel();
        handler = new DeleteHandler();
        metrics = mock(MetricsLogger.class);
    }

    @AfterEach
    public void tear_down() {
        verify(sdkClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(sdkClient);
    }

    @Test
    void handleRequest_ShouldReturnSuccess_When_DestinationIsDeleted() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        Mockito
            .when(proxyClient.client().deleteDestination(ArgumentMatchers.any(DeleteDestinationRequest.class)))
            .thenReturn(DeleteDestinationResponse.builder().build());
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
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    void handleRequest_ShouldThrowException_When_DestinationIsNotFound() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        Mockito
            .when(proxyClient.client().deleteDestination(ArgumentMatchers.any(DeleteDestinationRequest.class)))
            .thenThrow(ResourceNotFoundException.class);
        Assertions.assertThrows(
            CfnNotFoundException.class,
            () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics)
        );
    }
}
