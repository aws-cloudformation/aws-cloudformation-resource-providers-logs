package software.amazon.logs.destination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;

class BaseHandlerStdTest extends AbstractTestBase {

    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<CloudWatchLogsClient> proxyClient;
    private CloudWatchLogsClient sdkClient;

    BaseHandlerStd handler = new BaseHandlerStd() {
        @Override
        protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            AmazonWebServicesClientProxy proxy,
            ResourceHandlerRequest<ResourceModel> request,
            CallbackContext callbackContext,
            ProxyClient<CloudWatchLogsClient> proxyClient,
            Logger logger,
            MetricsLogger metrics
        ) {
            return null;
        }
    };

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(CloudWatchLogsClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @ParameterizedTest
    @MethodSource
    void alreadyExists(String destinationNameToCreate, String returnedDestinationName, boolean shouldExist) {
        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse
            .builder()
            .destinations(getTestDestination(returnedDestinationName))
            .build();

        when(proxyClient.client().describeDestinations(any(DescribeDestinationsRequest.class))).thenReturn(describeResponse);

        assertEquals(shouldExist, handler.exists(proxyClient, getTestResourceModel(destinationNameToCreate)));
    }

    private static Stream<Arguments> alreadyExists() {
        return Stream.of(
            Arguments.of("destination-name-suffix", "destination-name", false),
            Arguments.of("destination-name", "destination-name", true),
            Arguments.of("destination-name", "Destination-Name", false),
            Arguments.of("destination-name", "destination-name-suffix", false),
            Arguments.of("destination-name", "", false)
        );
    }
}
