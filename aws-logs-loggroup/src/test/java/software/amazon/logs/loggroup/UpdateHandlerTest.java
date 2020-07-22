package software.amazon.logs.loggroup;

import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteRetentionPolicyResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutRetentionPolicyResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.DisassociateKmsKeyResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.AssociateKmsKeyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DisassociateKmsKeyRequest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {
    UpdateHandler handler;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        handler = new UpdateHandler();
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void handleRequest_Success() {
        final PutRetentionPolicyResponse putRetentionPolicyResponse = PutRetentionPolicyResponse.builder().build();
        final LogGroup logGroup = LogGroup.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .build();
        final DescribeLogGroupsResponse describeResponse = DescribeLogGroupsResponse.builder()
                .logGroups(Collections.singletonList(logGroup))
                .build();

        doReturn(putRetentionPolicyResponse, describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                any(),
                any()
            );

        final ResourceModel model = ResourceModel.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel()).isEqualToComparingFieldByField(logGroup);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_Success_RetentionPolicyDeleted() {
        final DeleteRetentionPolicyResponse deleteRetentionPolicyResponse = DeleteRetentionPolicyResponse.builder().build();
        final LogGroup logGroup = LogGroup.builder()
            .logGroupName("LogGroup")
            .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .build();
        final DescribeLogGroupsResponse describeResponse = DescribeLogGroupsResponse.builder()
            .logGroups(Collections.singletonList(logGroup))
            .build();

        doReturn(deleteRetentionPolicyResponse, describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                any(),
                any()
            );

        final ResourceModel model = ResourceModel.builder()
            .logGroupName("LogGroup")
            .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel()).isEqualToComparingFieldByField(logGroup);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_Success_KmsKeyIdDeleted() {
        final DisassociateKmsKeyResponse disassociateKmsKeyResponse = DisassociateKmsKeyResponse.builder().build();
        final LogGroup logGroup = LogGroup.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .build();
        final DescribeLogGroupsResponse describeResponse = DescribeLogGroupsResponse.builder()
                .logGroups(Collections.singletonList(logGroup))
                .build();

        doReturn(disassociateKmsKeyResponse, describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2( any(), any());

        final ResourceModel model = ResourceModel.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel()).isEqualToComparingFieldByField(logGroup);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SuccessNoChange() {
        final LogGroup initialLogGroup = LogGroup.builder()
            .logGroupName("LogGroup")
            .retentionInDays(1)
            .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .build();
        final DescribeLogGroupsResponse initialDescribeResponse = DescribeLogGroupsResponse.builder()
            .logGroups(Collections.singletonList(initialLogGroup))
            .build();
        final LogGroup logGroup = LogGroup.builder()
            .logGroupName("LogGroup")
            .retentionInDays(1)
            .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .build();
        final DescribeLogGroupsResponse describeResponse = DescribeLogGroupsResponse.builder()
            .logGroups(Collections.singletonList(logGroup))
            .build();

        doReturn(initialDescribeResponse, describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                any(),
                any()
            );

        final ResourceModel model = ResourceModel.builder()
            .logGroupName("LogGroup")
            .retentionInDays(1)
            .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel()).isEqualToComparingFieldByField(logGroup);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SuccessNoChange_NoAction_WithPreviousModel() {
        final LogGroup logGroup = LogGroup.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .build();
        final ResourceModel previousModel = ResourceModel.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .build();

        final ResourceModel model = ResourceModel.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(previousModel)
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel()).isEqualToComparingFieldByField(logGroup);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_Success_UpdateWith_RetentionAndKms() {
        final LogGroup logGroup = LogGroup.builder()
            .logGroupName("LogGroup")
            .retentionInDays(2)
            .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .build();
        final ResourceModel previousModel = ResourceModel.builder()
            .logGroupName("LogGroup")
            .build();

        final ResourceModel model = ResourceModel.builder()
            .logGroupName("LogGroup")
            .retentionInDays(2)
            .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .previousResourceState(previousModel)
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        ArgumentCaptor<CloudWatchLogsRequest> requests = ArgumentCaptor.forClass(CloudWatchLogsRequest.class);
        verify(proxy, times(2)).injectCredentialsAndInvokeV2(requests.capture(), any());
        assertThat(requests.getAllValues().get(0)).isEqualTo(PutRetentionPolicyRequest.builder()
            .logGroupName("LogGroup")
            .retentionInDays(2)
            .build());

        assertThat(requests.getAllValues().get(1)).isEqualTo(AssociateKmsKeyRequest.builder()
            .logGroupName("LogGroup")
            .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .build());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel()).isEqualToComparingFieldByField(logGroup);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_FailureNotFound_ServiceException() {
        doThrow(software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException.class)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                any(),
                any()
            );

        final ResourceModel model = ResourceModel.builder()
            .logGroupName("LogGroup")
            .retentionInDays(1)
            .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(software.amazon.cloudformation.exceptions.ResourceNotFoundException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_PutRetention_FailureNotFound_ServiceException() {
        doThrow(software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.isA(PutRetentionPolicyRequest.class),
                        any()
                );

        final ResourceModel model = ResourceModel.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(software.amazon.cloudformation.exceptions.ResourceNotFoundException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_DeleteRetention_FailureNotFound_ServiceException() {
        doThrow(software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.isA(DeleteRetentionPolicyRequest.class),
                        any()
                );

        final ResourceModel model = ResourceModel.builder()
                .logGroupName("LogGroup")
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(software.amazon.cloudformation.exceptions.ResourceNotFoundException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_AssociateKms_FailureNotFound_ServiceException() {
        final PutRetentionPolicyResponse putRetentionPolicyResponse = PutRetentionPolicyResponse.builder().build();
        doReturn(putRetentionPolicyResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.isA(PutRetentionPolicyRequest.class),
                any()
            );
        doThrow(software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException.class)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.isA(AssociateKmsKeyRequest.class),
                any()
        );

        final ResourceModel model = ResourceModel.builder()
            .logGroupName("LogGroup")
            .retentionInDays(1)
            .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(software.amazon.cloudformation.exceptions.ResourceNotFoundException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_AssociateKms_InvalidParameter_ServiceException() {
        final PutRetentionPolicyResponse putRetentionPolicyResponse = PutRetentionPolicyResponse.builder().build();
        doReturn(putRetentionPolicyResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.isA(PutRetentionPolicyRequest.class),
                any()
            );
        doThrow(software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException.class)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.isA(AssociateKmsKeyRequest.class),
                any()
            );

        final ResourceModel model = ResourceModel.builder()
            .logGroupName("LogGroup")
            .retentionInDays(1)
            .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(software.amazon.cloudformation.exceptions.CfnInternalFailureException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_AssociateKms_OperationAborted_ServiceException() {
        final PutRetentionPolicyResponse putRetentionPolicyResponse = PutRetentionPolicyResponse.builder().build();
        doReturn(putRetentionPolicyResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.isA(PutRetentionPolicyRequest.class),
                any()
            );
        doThrow(software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException.class)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.isA(AssociateKmsKeyRequest.class),
                any()
            );

        final ResourceModel model = ResourceModel.builder()
            .logGroupName("LogGroup")
            .retentionInDays(1)
            .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(software.amazon.cloudformation.exceptions.CfnResourceConflictException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_AssociateKms_ServiceUnavailable_ServiceException() {
        final PutRetentionPolicyResponse putRetentionPolicyResponse = PutRetentionPolicyResponse.builder().build();
        doReturn(putRetentionPolicyResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.isA(PutRetentionPolicyRequest.class),
                any()
            );
        doThrow(software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException.class)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.isA(AssociateKmsKeyRequest.class),
                any()
            );

        final ResourceModel model = ResourceModel.builder()
            .logGroupName("LogGroup")
            .retentionInDays(1)
            .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_DisassociateKms_FailureNotFound_ServiceException() {
        final PutRetentionPolicyResponse putRetentionPolicyResponse = PutRetentionPolicyResponse.builder().build();
        doReturn(putRetentionPolicyResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.isA(PutRetentionPolicyRequest.class),
                any()
            );
        doThrow(software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException.class)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.isA(DisassociateKmsKeyRequest.class),
                any()
            );

        final ResourceModel model = ResourceModel.builder()
            .logGroupName("LogGroup")
            .retentionInDays(1)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(software.amazon.cloudformation.exceptions.ResourceNotFoundException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_DisassociateKms_InvalidParameter_ServiceException() {
        final PutRetentionPolicyResponse putRetentionPolicyResponse = PutRetentionPolicyResponse.builder().build();
        doReturn(putRetentionPolicyResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.isA(PutRetentionPolicyRequest.class),
                any()
            );
        doThrow(software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException.class)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.isA(DisassociateKmsKeyRequest.class),
                any()
            );

        final ResourceModel model = ResourceModel.builder()
            .logGroupName("LogGroup")
            .retentionInDays(1)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(software.amazon.cloudformation.exceptions.CfnInternalFailureException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_DisassociateKms_OperationAborted_ServiceException() {
        final PutRetentionPolicyResponse putRetentionPolicyResponse = PutRetentionPolicyResponse.builder().build();
        doReturn(putRetentionPolicyResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.isA(PutRetentionPolicyRequest.class),
                any()
            );
        doThrow(software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException.class)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.isA(DisassociateKmsKeyRequest.class),
                any()
            );

        final ResourceModel model = ResourceModel.builder()
            .logGroupName("LogGroup")
            .retentionInDays(1)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(software.amazon.cloudformation.exceptions.CfnResourceConflictException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_DisassociateKms_ServiceUnavailable_ServiceException() {
        final PutRetentionPolicyResponse putRetentionPolicyResponse = PutRetentionPolicyResponse.builder().build();
        doReturn(putRetentionPolicyResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.isA(PutRetentionPolicyRequest.class),
                any()
            );
        doThrow(software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException.class)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.isA(DisassociateKmsKeyRequest.class),
                any()
            );

        final ResourceModel model = ResourceModel.builder()
            .logGroupName("LogGroup")
            .retentionInDays(1)
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }
}
