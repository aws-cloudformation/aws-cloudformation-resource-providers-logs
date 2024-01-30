package software.amazon.logs.destination;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;

@ExtendWith(MockitoExtension.class)
class ListHandlerTest extends AbstractTestBase {

    @Mock
    private CloudWatchLogsClient sdkClient;

    @Mock
    private MetricsLogger metrics;

    private AmazonWebServicesClientProxy proxy;

    private ProxyClient<CloudWatchLogsClient> proxyClient;

    private ResourceModel testResourceModel;

    private ListHandler handler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        testResourceModel = getTestResourceModel();
        handler = new ListHandler();
        metrics = mock(MetricsLogger.class);
    }

    @Test
    void handleRequest_ShouldReturnSuccess_When_DestinationIsFound() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(testResourceModel)
            .build();
        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse
            .builder()
            .destinations(getTestDestination())
            .build();

        Mockito.when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class))).thenReturn(describeResponse);

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
        Assertions.assertThat(response.getCallbackContext()).isNull();
        Assertions.assertThat(response.getCallbackDelaySeconds()).isZero();
        Assertions.assertThat(response.getResourceModel()).isNull();
        Assertions.assertThat(response.getResourceModels()).isNotNull();
        Assertions.assertThat(response.getMessage()).isNull();
        Assertions.assertThat(response.getErrorCode()).isNull();
    }
}
