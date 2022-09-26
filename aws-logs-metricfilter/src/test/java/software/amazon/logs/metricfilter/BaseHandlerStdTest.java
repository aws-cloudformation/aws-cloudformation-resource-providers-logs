package software.amazon.logs.metricfilter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.RetryableException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class BaseHandlerStdTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<CloudWatchLogsClient> proxyClient;

    @Mock
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

    @Test
    void handleRateExceededError() {
        final ResourceModel model = buildDefaultModel();

        AwsServiceException exception = AwsServiceException.builder()
                .statusCode(400)
                .requestId("e689f8f9-bc25-48de-86be-4cee73125707")
                .awsErrorDetails(AwsErrorDetails.builder()
                        .serviceName("CloudWatchLogs")
                        .errorMessage("Rate exceeded")
                        .build())
                .build();

        PutMetricFilterRequest request = PutMetricFilterRequest.builder().build();

        RetryableException thrown = assertThrows(
                RetryableException.class,
                () -> handler.handleRateExceededError.invoke(request, exception, proxyClient, model, new CallbackContext())
        );

        assertTrue(thrown.getMessage().contains("Rate exceeded"));
    }

    @Test
    void handleNotRateExceededError() {
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

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = assertDoesNotThrow(
                () -> handler.handleRateExceededError.invoke(putMetricFilterRequest, exception, proxyClient, model, new CallbackContext())
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertEquals(response.getMessage(), "Invalid request (Service: CloudWatchLogs, Status Code: 400, Request ID: e689f8f9-bc25-48de-86be-4cee73125707)");
        assertThat(response.getErrorCode()).isNotNull();
        verify(proxyClient.client(), never()).putMetricFilter(any(PutMetricFilterRequest.class));
    }
}