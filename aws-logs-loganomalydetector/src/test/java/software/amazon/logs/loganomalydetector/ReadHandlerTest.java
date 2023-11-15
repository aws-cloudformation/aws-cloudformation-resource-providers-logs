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
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogAnomalyDetectorResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.GetLogAnomalyDetectorRequest;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.HashSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.mockito.ArgumentMatchers;


@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<CloudWatchLogsClient> proxyClient;

    @Mock
    CloudWatchLogsClient cloudWatchLogsClient;

    private ResourceModel model;
    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        cloudWatchLogsClient = mock(CloudWatchLogsClient.class);
        proxyClient = MOCK_PROXY(proxy, cloudWatchLogsClient);
    }


    @Test
    public void handleRequest_SimpleSuccess() {
        final ReadHandler handler = new ReadHandler();

        final HashSet<String> logGroupArnList = new HashSet<String>();
        logGroupArnList.add("arn:aws:logs:us-east-1:123456789012:log-group:test");

        final ResourceModel model = ResourceModel.builder()
                .anomalyDetectorArn("arn:aws:logs:us-east-1:123456789012:anomaly-detector/12345678901234567890123456789012")
                .detectorName("TestDetector")
                .anomalyDetectorStatus("INITIALIZING")
                .anomalyVisibilityTime((double) 7)
                .filterPattern("FP")
                .kmsKeyId("1234")
                .creationTimeStamp((double)123)
                .evaluationFrequency("FIVE_MIN")
                .lastModifiedTimeStamp((double)123)
                .logGroupArnList(logGroupArnList)
                .build();

        final GetLogAnomalyDetectorResponse getLogAnomalyDetectorResponse = GetLogAnomalyDetectorResponse.builder()
                .detectorName("TestDetector")
                .anomalyDetectorStatus("INITIALIZING")
                .anomalyVisibilityTime((long) 7)
                .creationTimeStamp((long)123)
                .evaluationFrequency("FIVE_MIN")
                .filterPattern("FP")
                .kmsKeyId("1234")
                .lastModifiedTimeStamp((long)123)
                .logGroupArnList("arn:aws:logs:us-east-1:123456789012:log-group:test")
                .build();

        when(proxyClient.client().getLogAnomalyDetector(any(GetLogAnomalyDetectorRequest.class)))
                .thenReturn(getLogAnomalyDetectorResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
