package software.amazon.logs.logstream;

import java.time.Duration;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

//@ExtendWith(MockitoExtension.class)
//public class DeleteHandlerTest extends AbstractTestBase {
//
//    @Mock
//    private AmazonWebServicesClientProxy proxy;
//
//    @Mock
//    private ProxyClient<CloudWatchLogsClient> proxyClient;
//
//    @Mock
//    CloudWatchLogsClient sdkClient;
//
//    final DeleteHandler handler = new DeleteHandler();
//
//    @BeforeEach
//    public void setup() {
//        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
//        sdkClient = mock(CloudWatchLogsClient.class);
//        proxyClient = MOCK_PROXY(proxy, sdkClient);
//    }
//
//    @AfterEach
//    public void tear_down() {
//        verify(sdkClient, atLeastOnce()).serviceName();
//        verifyNoMoreInteractions(sdkClient);
//    }
//
//    @Test
//    public void handleRequest_SimpleSuccess() {
//
//        final ResourceModel model = ResourceModel.builder()
//                .logGroupName("logGroupName1")
//                .logStreamName("logStreamName")
//                .build();
//
//
//
//        when(proxyClient.client().deleteLogStream(any(DeleteLogStreamRequest.class)))
//                .thenReturn(DeleteLogStreamResponse.builder().build())
//                .thenReturn(DeleteLogStreamResponse.builder().build());
//
//        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
//            .desiredResourceState(model)
//            .build();
//
//        final ProgressEvent<ResourceModel, CallbackContext> response =
//                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
//
//        assertThat(response).isNotNull();
//        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
//        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
//        assertThat(response.getResourceModel()).isNull();
//        assertThat(response.getResourceModels()).isNull();
//        assertThat(response.getMessage()).isNull();
//        assertThat(response.getErrorCode()).isNull();
//    }
//}
