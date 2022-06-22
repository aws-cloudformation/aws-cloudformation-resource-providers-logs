package software.amazon.logs.logstream;

import java.time.Duration;
import java.util.Collections;

import software.amazon.awssdk.core.SdkClient;
import org.junit.jupiter.api.Tag;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.*;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import software.amazon.awssdk.services.cloudwatchlogs.model.*;
import software.amazon.cloudformation.proxy.*;

import software.amazon.cloudformation.exceptions.*;
import com.amazonaws.services.kms.model.AlreadyExistsException;
//import com.amazonaws.services.kms.model.LimitExceededException;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceAlreadyExistsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;
import software.amazon.cloudformation.exceptions.*;

import static org.assertj.core.api.Assertions.*;
import static software.amazon.logs.logstream.Translator.translateException;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
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
    CloudWatchLogsClient sdkClient;

    final CreateHandler handler = new CreateHandler();

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(CloudWatchLogsClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @AfterEach
    public void tear_down(org.junit.jupiter.api.TestInfo testInfo) {
        if (testInfo.getTags().contains("noSdkInteraction")) {
            verify(sdkClient, never()).serviceName();
        } else {
            verify(sdkClient, atLeastOnce()).serviceName();
        }
        verifyNoMoreInteractions(sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceModel model = ResourceModel.builder()
                .logGroupName("logGroupName1")
                .logStreamName("logStreamName")
                .build();

        final DescribeLogStreamsResponse describeResponse = DescribeLogStreamsResponse.builder()
                .logStreams(Translator.translateToSDK(model))
                .build();

        final CreateLogStreamResponse createResponse = CreateLogStreamResponse.builder()
                .build();

        when(proxyClient.client().describeLogStreams(any(DescribeLogStreamsRequest.class)))
                .thenReturn(DescribeLogStreamsResponse.builder().build())
                .thenReturn(describeResponse);

        when(proxyClient.client().createLogStream(any(CreateLogStreamRequest.class)))
                .thenReturn(createResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(10);

        final ProgressEvent<ResourceModel, CallbackContext> response2 =
                handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response2).isNotNull();
        assertThat(response2.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response2.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response2.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response2.getResourceModels()).isNull();
        assertThat(response2.getMessage()).isNull();
        assertThat(response2.getErrorCode()).isNull();
        verify(proxyClient.client(), times(2)).describeLogStreams(any(DescribeLogStreamsRequest.class));
        verify(proxyClient.client()).createLogStream(any(CreateLogStreamRequest.class));
    }


    @Tag("noSdkInteraction")
    @Test
    public void handleRequest_LogGroupNameEmpty() {
        final ResourceModel model = ResourceModel.builder()
                .logStreamName("logStreamName")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);

    }


    @Tag("noSdkInteraction")
    @Test
    public void handleRequest_ModelisEmpty() {
        final ResourceModel model = ResourceModel.builder()
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(null)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);

    }


//    @Test
//    public void handleRequest_LogStreamNameGenerated() {
//        final ResourceModel model = ResourceModel.builder()
//                .logGroupName("logGroupName1")
//                .build();
//
//        final ResourceModel model2 = ResourceModel.builder()
//                .logGroupName("logGroupName2")
//                .build();
//
//        final DescribeLogStreamsResponse describeResponse = DescribeLogStreamsResponse.builder()
//                .logStreams(Translator.translateToSDK(model))
//                .build();
//
//        final CreateLogStreamResponse createResponse = CreateLogStreamResponse.builder()
//                .build();
//
//        when(proxyClient.client().describeLogStreams(any(DescribeLogStreamsRequest.class)))
//                .thenReturn(DescribeLogStreamsResponse.builder().build())
//                .thenReturn(describeResponse);
//
//        when(proxyClient.client().createLogStream(any(CreateLogStreamRequest.class)))
//                .thenReturn(createResponse);
//
//        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
//                .desiredResourceState(model)
//                .build();
//
//        final ProgressEvent<ResourceModel, CallbackContext> response =
//                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
//        assertThat(response).isNotNull();
//        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
//        assertThat(response.getCallbackDelaySeconds()).isEqualTo(10);
//
//        final ProgressEvent<ResourceModel, CallbackContext> response2 =
//                handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);
//
//        assertThat(response2).isNotNull();
//        assertThat(response2.getStatus()).isEqualTo(OperationStatus.SUCCESS);
//        assertThat(response2.getCallbackDelaySeconds()).isEqualTo(0);
//        assertThat(response2.getResourceModel()).isEqualTo(request.getDesiredResourceState());
//        assertThat(response2.getResourceModels()).isNull();
//        assertThat(response2.getMessage()).isNull();
//        assertThat(response2.getErrorCode()).isNull();
//        verify(proxyClient.client(), times(2)).describeLogStreams(any(DescribeLogStreamsRequest.class));
//        verify(proxyClient.client()).createLogStream(any(CreateLogStreamRequest.class));
//    }

    @Tag("noSdkInteraction")
    @Test
    public void handleRequest_LogStreamNameContainsColon() {
        final ResourceModel model = ResourceModel.builder()
                .logGroupName("logGroupName")
                .logStreamName("logstream:name")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);

    }

    @Tag("noSdkInteraction")
    @Test
    public void handleRequest_LogStreamNameContainsAsterisk() {
        final ResourceModel model = ResourceModel.builder()
                .logGroupName("logGroupName")
                .logStreamName("logstream*name")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);

    }

    @Tag("noSdkInteraction")
    @Test
    public void handleRequest_ModelEmpty() {
        final ResourceModel model = ResourceModel.builder()
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);

    }

    @Tag("noSdkInteraction")
    @Test
    public void handleRequest_FailedCreate_AlreadyExists() {
        final ResourceModel model = ResourceModel.builder()
                .logGroupName("logGroupName1")
                .logStreamName("logStreamName")
                .build();

        final DescribeLogStreamsResponse describeResponse = DescribeLogStreamsResponse.builder()
                .logStreams(Translator.translateToSDK(model))
                .build();

        when(proxyClient.client().describeLogStreams(any(DescribeLogStreamsRequest.class)))
                .thenReturn(describeResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler
                .handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.AlreadyExists);
    }

    @Test
    public void handleRequest_DoesResourceExistwithName() {
        final ResourceModel model = ResourceModel.builder()
                .logGroupName("logGroupName1")
                .logStreamName("logStreamName")
                .build();

        final ResourceModel model2 = ResourceModel.builder()
                .logGroupName("logGroupName1")
                .logStreamName("logStreamName2")
                .build();

        final DescribeLogStreamsResponse describeResponse = DescribeLogStreamsResponse.builder()
                .logStreams(Translator.translateToSDK(model2))
                .build();

        final DescribeLogStreamsResponse describeResponse2 = DescribeLogStreamsResponse.builder()
                .logStreams(Translator.translateToSDK(model))
                .build();

        when(proxyClient.client().describeLogStreams(any(DescribeLogStreamsRequest.class)))
                .thenReturn(describeResponse)
                .thenReturn(describeResponse2);

        final CreateLogStreamResponse createResponse = CreateLogStreamResponse.builder()
                .build();

//        when(proxyClient.client().describeLogStreams(any(DescribeLogStreamsRequest.class)))
//                .thenReturn(DescribeLogStreamsResponse.builder().build())
//                .thenReturn(describeResponse);

        when(proxyClient.client().createLogStream(any(CreateLogStreamRequest.class)))
                .thenReturn(createResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler
                .handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(10);
        assertThat(response.getErrorCode()).isNull();

        final ProgressEvent<ResourceModel, CallbackContext> response2 =
                handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response2).isNotNull();
        assertThat(response2.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response2.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response2.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response2.getResourceModels()).isNull();
        assertThat(response2.getMessage()).isNull();
        assertThat(response2.getErrorCode()).isNull();
        verify(proxyClient.client(), times(2)).describeLogStreams(any(DescribeLogStreamsRequest.class));
        verify(proxyClient.client()).createLogStream(any(CreateLogStreamRequest.class));

    }

    @Test
    public void handleRequest_DoesResourceExistwithNameException() {
        final ResourceModel model = ResourceModel.builder()
                .logGroupName("logGroupName1")
                .logStreamName("logStreamName")
                .build();

        final ResourceModel model2 = ResourceModel.builder()
                .logGroupName("logGroupName1")
                .logStreamName("logStreamName2")
                .build();

        final DescribeLogStreamsResponse describeResponse = DescribeLogStreamsResponse.builder()
                .logStreams(Translator.translateToSDK(model2))
                .build();

        final DescribeLogStreamsResponse describeResponse2 = DescribeLogStreamsResponse.builder()
                .logStreams(Translator.translateToSDK(model))
                .build();

        when(proxyClient.client().describeLogStreams(any(DescribeLogStreamsRequest.class)))
                .thenThrow(ResourceNotFoundException.class)
                .thenReturn(describeResponse2);

        final CreateLogStreamResponse createResponse = CreateLogStreamResponse.builder()
                .build();

//        when(proxyClient.client().describeLogStreams(any(DescribeLogStreamsRequest.class)))
//                .thenReturn(DescribeLogStreamsResponse.builder().build())
//                .thenReturn(describeResponse);

        when(proxyClient.client().createLogStream(any(CreateLogStreamRequest.class)))
                .thenReturn(createResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler
                .handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(10);
        assertThat(response.getErrorCode()).isNull();

        final ProgressEvent<ResourceModel, CallbackContext> response2 =
                handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response2).isNotNull();
        assertThat(response2.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response2.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response2.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response2.getResourceModels()).isNull();
        assertThat(response2.getMessage()).isNull();
        assertThat(response2.getErrorCode()).isNull();
        verify(proxyClient.client(), times(2)).describeLogStreams(any(DescribeLogStreamsRequest.class));
        verify(proxyClient.client()).createLogStream(any(CreateLogStreamRequest.class));

    }

    @Test
    public void handleRequest_FailedCreate_ServiceUnavailable() {
        final ResourceModel model = ResourceModel.builder()
                .logGroupName("logGroupName1")
                .logStreamName("logStreamName")
                .build();

        final DescribeLogStreamsResponse describeResponse = DescribeLogStreamsResponse.builder()
                .logStreams(Translator.translateToSDK(model))
                .build();

        final CreateLogStreamResponse createResponse = CreateLogStreamResponse.builder()
                .build();


        when(proxyClient.client().describeLogStreams(any(DescribeLogStreamsRequest.class)))
                .thenReturn(DescribeLogStreamsResponse.builder().build())
                .thenReturn(describeResponse);

        when(proxyClient.client().createLogStream(any(CreateLogStreamRequest.class)))
                .thenThrow(ServiceUnavailableException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler
                .handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceInternalError);
    }

    @Test
    public void handleRequest_FailedCreate_ServiceLimitExceeded() {
        final ResourceModel model = ResourceModel.builder()
                .logGroupName("logGroupName1")
                .logStreamName("logStreamName")
                .build();

        final DescribeLogStreamsResponse describeResponse = DescribeLogStreamsResponse.builder()
                .logStreams(Translator.translateToSDK(model))
                .build();

        final CreateLogStreamResponse createResponse = CreateLogStreamResponse.builder()
                .build();


        when(proxyClient.client().describeLogStreams(any(DescribeLogStreamsRequest.class)))
                .thenReturn(DescribeLogStreamsResponse.builder().build())
                .thenReturn(describeResponse);

        when(proxyClient.client().createLogStream(any(CreateLogStreamRequest.class)))
                .thenThrow(LimitExceededException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler
                .handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.ServiceLimitExceeded);
    }

    @Test
    public void handleRequest_FailedCreate_GeneralException() {
        final ResourceModel model = ResourceModel.builder()
                .logGroupName("logGroupName1")
                .logStreamName("logStreamName")
                .build();

        final DescribeLogStreamsResponse describeResponse = DescribeLogStreamsResponse.builder()
                .logStreams(Translator.translateToSDK(model))
                .build();

        final CreateLogStreamResponse createResponse = CreateLogStreamResponse.builder()
                .build();


        when(proxyClient.client().describeLogStreams(any(DescribeLogStreamsRequest.class)))
                .thenReturn(DescribeLogStreamsResponse.builder().build())
                .thenReturn(describeResponse);

        when(proxyClient.client().createLogStream(any(CreateLogStreamRequest.class)))
                .thenThrow(AwsServiceException.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler
                .handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.GeneralServiceException);
    }




    @Tag("noSdkInteraction")
    @Test
    public void testExceptionTranslation() {
        final Exception e = new Exception();
        final LimitExceededException limitExceededException = LimitExceededException.builder().build();
        final CfnServiceLimitExceededException cfnServiceLimitExceededException = new CfnServiceLimitExceededException(e);
        assertThat(translateException(limitExceededException)).isEqualToComparingFieldByField(cfnServiceLimitExceededException);

        final OperationAbortedException operationAbortedException = OperationAbortedException.builder().build();
        final CfnResourceConflictException cfnResourceConflictException = new CfnResourceConflictException(e);
        assertThat(translateException(operationAbortedException)).isEqualToComparingFieldByField(cfnResourceConflictException);

        final InvalidParameterException invalidParameterException = InvalidParameterException.builder().build();
        final CfnInvalidRequestException cfnInvalidRequestException = new CfnInvalidRequestException(e);
        assertThat(translateException(invalidParameterException)).isEqualToComparingFieldByField(cfnInvalidRequestException);

        final ResourceNotFoundException resourceNotFoundException = ResourceNotFoundException.builder().build();
        final CfnNotFoundException cfnNotFoundException = new CfnNotFoundException(e);
        assertThat(translateException(resourceNotFoundException)).isEqualToComparingFieldByField(cfnNotFoundException);

        final ServiceUnavailableException serviceUnavailableException = ServiceUnavailableException.builder().build();
        final CfnServiceInternalErrorException cfnServiceInternalErrorException = new CfnServiceInternalErrorException(e);
        assertThat(translateException(serviceUnavailableException)).isEqualToComparingFieldByField(cfnServiceInternalErrorException);

        final AwsServiceException awsServiceException = AwsServiceException.builder().build();
        CfnGeneralServiceException cfnGeneralServiceException = new CfnGeneralServiceException(e);
        assertThat(translateException(awsServiceException)).isEqualToComparingFieldByField(cfnGeneralServiceException);
    }







}
