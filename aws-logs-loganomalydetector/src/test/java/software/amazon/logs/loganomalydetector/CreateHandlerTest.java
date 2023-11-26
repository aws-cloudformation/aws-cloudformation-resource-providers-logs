package software.amazon.logs.loganomalydetector;

import java.time.Duration;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogAnomalyDetectorResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogAnomalyDetectorRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogAnomalyDetectorResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogAnomalyDetectorRequest;
import java.util.HashSet;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<CloudWatchLogsClient> proxyClient;

    @Mock
    CloudWatchLogsClient cloudWatchLogsClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        cloudWatchLogsClient = mock(CloudWatchLogsClient.class);
        proxyClient = MOCK_PROXY(proxy, cloudWatchLogsClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final CreateHandler handler = new CreateHandler();
        final HashSet<String> logGroupArnList = new HashSet<String>();
        logGroupArnList.add("arn:aws:logs:us-east-1:123456789012:log-group:test");

        final ResourceModel finalModel = ResourceModel.builder()
            .anomalyDetectorArn("arn:aws:logs:us-east-1:123456789012:anomaly-detector/12345678901234567890123456789012")
            .build();

        final ResourceModel createModel = ResourceModel.builder()
            .detectorName("TestDetector")
            .anomalyVisibilityTime((double) 7)
            .evaluationFrequency("FIVE_MIN")
            .filterPattern("FP")
            .kmsKeyId("1234")
            .logGroupArnList(logGroupArnList)
            .build();

        final CreateLogAnomalyDetectorResponse createLogAnomalyDetectorResponse = CreateLogAnomalyDetectorResponse.builder()
                .anomalyDetectorArn("arn:aws:logs:us-east-1:123456789012:anomaly-detector/12345678901234567890123456789012")
                .build();

        final ResourceHandlerRequest<ResourceModel> createRequest = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(createModel)
            .awsAccountId("123456789012")
            .build();

        when(proxyClient.client().createLogAnomalyDetector(any(CreateLogAnomalyDetectorRequest.class)))
                .thenReturn(createLogAnomalyDetectorResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, createRequest, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(finalModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
