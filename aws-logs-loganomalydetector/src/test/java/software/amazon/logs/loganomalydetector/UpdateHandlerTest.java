package software.amazon.logs.loganomalydetector;

import java.time.Duration;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
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
import software.amazon.awssdk.services.cloudwatchlogs.model.UpdateLogAnomalyDetectorResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.UpdateLogAnomalyDetectorRequest;
import java.util.HashSet;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

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
        final UpdateHandler handler = new UpdateHandler();
        final HashSet<String> logGroupArnList = new HashSet<String>();
        logGroupArnList.add("arn:aws:logs:us-east-1:123456789012:log-group:test");

        final ResourceModel updateModel = ResourceModel.builder()
                .anomalyDetectorArn("arn:aws:logs:us-east-1:123456789012:anomaly-detector/12345678901234567890123456789012")
                .anomalyVisibilityTime((double) 7)
                .evaluationFrequency("ONE_HOUR")
                .filterPattern("NewFP")
                .kmsKeyId("5678")
                .build();

        final UpdateLogAnomalyDetectorResponse updateLogAnomalyDetectorResponse = UpdateLogAnomalyDetectorResponse.builder()
                .build();

        final ResourceHandlerRequest<ResourceModel> updateRequest = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(updateModel)
            .build();

        when(proxyClient.client().updateLogAnomalyDetector(any(UpdateLogAnomalyDetectorRequest.class)))
                .thenReturn(updateLogAnomalyDetectorResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, updateRequest, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(updateModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
