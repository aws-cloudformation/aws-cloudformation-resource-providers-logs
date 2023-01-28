package software.amazon.logs.destination;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaseHandlerStdTest extends AbstractTestBase {

    BaseHandlerStd handler = new BaseHandlerStd() {
        @Override
        protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, ProxyClient<CloudWatchLogsClient> proxyClient, Logger logger) {
            return null;
        }
    };

    @ParameterizedTest
    @MethodSource
    void alreadyExists(String destinationNameToCreate, String returnedDestinationName, boolean shouldExist) {
        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse.builder()
                .destinations(getTestDestination(returnedDestinationName))
                .build();

        assertEquals(shouldExist, handler.destinationNameExists(describeResponse, getTestResourceModel(destinationNameToCreate)));
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