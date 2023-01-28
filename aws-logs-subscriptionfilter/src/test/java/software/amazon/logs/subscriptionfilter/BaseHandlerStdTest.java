package software.amazon.logs.subscriptionfilter;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersResponse;
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
    void alreadyExists(String subscriptionNameToCreate, String returnedSubscriptionName, boolean shouldExist) {
        final ResourceModel model = buildDefaultModel(returnedSubscriptionName);
        final DescribeSubscriptionFiltersResponse response = DescribeSubscriptionFiltersResponse.builder()
                .subscriptionFilters(Translator.translateToSDK(model))
                .build();

        assertEquals(shouldExist, handler.filterNameExists(response, buildDefaultModel(subscriptionNameToCreate)));
    }

    private static Stream<Arguments> alreadyExists() {
        return Stream.of(
                Arguments.of("subscription-name-suffix", "subscription-name", false),
                Arguments.of("subscription-name", "subscription-name", true),
                Arguments.of("subscription-name", "Subscription-Name", false),
                Arguments.of("subscription-name", "subscription-name-suffix", false),
                Arguments.of("subscription-name", "", false)
        );
    }
}