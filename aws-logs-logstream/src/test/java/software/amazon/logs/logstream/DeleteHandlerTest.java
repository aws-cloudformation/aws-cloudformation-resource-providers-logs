package software.amazon.logs.logstream;

import java.time.Duration;
import com.google.common.collect.ImmutableList;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
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
import software.amazon.cloudformation.proxy.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<CloudWatchLogsClient> proxyClient;

    @Mock
    CloudWatchLogsClient sdkClient;

    final DeleteHandler handler = new DeleteHandler();

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(CloudWatchLogsClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @AfterEach
    public void tear_down() {
        verify(sdkClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {

        final ResourceModel model = ResourceModel.builder()
                .logGroupName("logGroupName1")
                .logStreamName("logStreamName")
                .build();

        when(proxyClient.client().deleteLogStream(any(DeleteLogStreamRequest.class)))
                .thenReturn(DeleteLogStreamResponse.builder().build())
                .thenReturn(DeleteLogStreamResponse.builder().build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(10);
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        final ProgressEvent<ResourceModel, CallbackContext> response2 =
                handler.handleRequest(proxy, request,response.getCallbackContext(), proxyClient, logger);

        assertThat(response2).isNotNull();
        assertThat(response2.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response2.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response2.getResourceModel()).isNull();
        assertThat(response2.getResourceModels()).isNull();
        assertThat(response2.getMessage()).isNull();
        assertThat(response2.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_ResourceNotFound() {
        final ResourceModel model = ResourceModel.builder()
                .logGroupName("logGroupName1")
                .logStreamName("logStreamName")
                .build();

        when(proxyClient.client().deleteLogStream(any(DeleteLogStreamRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }

    @Test
    public void handleRequest_ResourceLimitExceeded() {
        final ResourceModel model = ResourceModel.builder()
                .logGroupName("logGroupName1")
                .logStreamName("logStreamName")
                .build();

        when(proxyClient.client().deleteLogStream(any(DeleteLogStreamRequest.class)))
                .thenThrow(LimitExceededException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceLimitExceeded);
    }

    @Test
    public void handleRequest_DeleteFailed() {
        final ResourceModel model = ResourceModel.builder()
                .logGroupName("logGroupName1")
                .logStreamName("logStreamName")
                .build();

        when(proxyClient.client().deleteLogStream(any(DeleteLogStreamRequest.class)))
                .thenThrow(InvalidParameterException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }


//    @Test
//    @org.junit.jupiter.api.Tag("SkipTearDown")
//    public void handleRequest_ExceptionList() {
//
//        CloudWatchLogsException e1 = (CloudWatchLogsException) CloudWatchLogsException.builder().awsErrorDetails(AwsErrorDetails.builder().errorCode("AlreadyExistsException").build()).build();
//        CloudWatchLogsException e2 = (CloudWatchLogsException) CloudWatchLogsException.builder().awsErrorDetails(AwsErrorDetails.builder().errorCode("InvalidParameterException").build()).build();
//        CloudWatchLogsException e3 = (CloudWatchLogsException) CloudWatchLogsException.builder().awsErrorDetails(AwsErrorDetails.builder().errorCode("ServiceUnavailable").build()).build();
//        CloudWatchLogsException e4 = (CloudWatchLogsException) CloudWatchLogsException.builder().awsErrorDetails(AwsErrorDetails.builder().errorCode("UnauthorizedOperation").build()).build();
//
//        List<Exception> exceptionList = ImmutableList.of(e1, e2, e3, e4);
//
//        for(int i = 0; i < exceptionList.size(); i++) {
//
//            LogStream gateway1 = LogStream.builder().logStreamName("logStreamName").build();
//
//            final DescribeLogStreamsResponse describeResponse = DescribeLogStreamsResponse.builder().logStreams(gateway1).build();
//            when(proxyClient.client().describeLogStreams(any(DescribeLogStreamsRequest.class))).thenReturn(describeResponse);
//
//            when(proxyClient.client().deleteLogStream(any(DeleteLogStreamRequest.class))).thenThrow(exceptionList.get(i));
//
//            final ResourceModel model = ResourceModel.builder()
//                    .logGroupName("logGroupName1")
//                    .logStreamName("logStreamName")
//                    .build();
//
//            final ResourceHandlerRequest<ResourceModel> request =
//                    ResourceHandlerRequest.<ResourceModel>builder().desiredResourceState(model).build();
//
//            final ProgressEvent<ResourceModel, CallbackContext> response =
//                    handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
//
//            assertThat(response).isNotNull();
//            assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
//            assertThat(response.getResourceModels()).isNull();
//            if (i == 0) {
//                assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
//                assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.AlreadyExists);
//            } else if (i == 1) {
//                assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
//                assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
//            } else if (i ==2) {
//                assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
//                assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
//            } else if (i == 3) {
//                assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
//                assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.AccessDenied);
//            }
//        }
//    }





}
