package software.amazon.logs.metricfilter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.RetryableException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.MetricFilter;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BaseHandlerStdTest extends AbstractTestBase {

    private AmazonWebServicesClientProxy proxy;

    private ProxyClient<CloudWatchLogsClient> proxyClient;

    CloudWatchLogsClient sdkClient;

    BaseHandlerStd handler = new BaseHandlerStd() {
        @Override
        protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy, ResourceHandlerRequest<ResourceModel> request, CallbackContext callbackContext, ProxyClient<CloudWatchLogsClient> proxyClient, Logger logger) {
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
    void handleRetryableErrors(int statusCode, String errorMessage) {
        final ResourceModel model = buildDefaultModel();

        AwsServiceException exception = AwsServiceException.builder()
                .statusCode(statusCode)
                .requestId("e689f8f9-bc25-48de-86be-4cee73125707")
                .awsErrorDetails(AwsErrorDetails.builder()
                        .serviceName("CloudWatchLogs")
                        .errorMessage(errorMessage)
                        .build())
                .build();

        PutMetricFilterRequest request = PutMetricFilterRequest.builder().build();

        RetryableException thrown = assertThrows(
                RetryableException.class,
                () -> handler.handleError.invoke(request, exception, proxyClient, model, new CallbackContext())
        );

        assertTrue(thrown.getMessage().contains(errorMessage));
    }

    private static Stream<Arguments> handleRetryableErrors() {
        return Stream.of(
                Arguments.of(400, "Rate exceeded"),
                Arguments.of(500, "Internal failure"),
                Arguments.of(502, "Bad gateway"),
                Arguments.of(503, "Service unavailable"),
                Arguments.of(504, "Gateway timeout")
        );
    }

    @Test
    void handleNonRetryableError() {
        final ResourceModel model = buildDefaultModel();

        AwsServiceException exception = AwsServiceException.builder()
                .statusCode(400)
                .requestId("e689f8f9-bc25-48de-86be-4cee73125707")
                .awsErrorDetails(AwsErrorDetails.builder()
                        .serviceName("CloudWatchLogs")
                        .errorMessage("Invalid request")
                        .build())
                .build();

        PutMetricFilterRequest putMetricFilterRequest = PutMetricFilterRequest.builder().build();

        final ProgressEvent<ResourceModel, CallbackContext> response = assertDoesNotThrow(
                () -> handler.handleError.invoke(putMetricFilterRequest, exception, proxyClient, model, new CallbackContext())
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getMessage()).contains("Invalid request (Service: CloudWatchLogs, Status Code: 400, Request ID: e689f8f9-bc25-48de-86be-4cee73125707");
        assertThat(response.getErrorCode()).isNotNull();
        verify(proxyClient.client(), never()).putMetricFilter(any(PutMetricFilterRequest.class));
    }

    @ParameterizedTest
    @MethodSource
    void exists(String filterNameToCreate, String returnedFilterName, boolean shouldExist) {
        ResourceModel model = ResourceModel.builder()
                .filterName(filterNameToCreate)
                .filterPattern("some pattern")
                .logGroupName("log-group-name")
                .build();

        final DescribeMetricFiltersResponse preCreateResponse = DescribeMetricFiltersResponse.builder()
                .metricFilters(returnedFilterName.isEmpty() ? Collections.emptyList() : Collections.singleton(MetricFilter.builder()
                        .filterName(returnedFilterName)
                        .filterPattern("some pattern")
                        .logGroupName("log-group-name")
                        .build()))
                .build();

        when(proxyClient.client().describeMetricFilters(any(DescribeMetricFiltersRequest.class)))
                .thenReturn(preCreateResponse);

        assertEquals(shouldExist, handler.exists(proxyClient, model));
    }

    private static Stream<Arguments> exists() {
        return Stream.of(
                Arguments.of("filter-name-suffix", "filter-name", false),
                Arguments.of("filter-name", "filter-name", true),
                Arguments.of("filter-name", "Filter-Name", false),
                Arguments.of("filter-name", "filter-name-suffix", false),
                Arguments.of("filter-name", "", false)
        );
    }
}
