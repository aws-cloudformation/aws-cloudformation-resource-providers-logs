package com.aws.logs.loggroup;

import com.amazonaws.cloudformation.exceptions.CfnAlreadyExistsException;
import com.amazonaws.cloudformation.proxy.AmazonWebServicesClientProxy;
import com.amazonaws.cloudformation.proxy.Logger;
import com.amazonaws.cloudformation.proxy.OperationStatus;
import com.amazonaws.cloudformation.proxy.ProgressEvent;
import com.amazonaws.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutRetentionPolicyResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceAlreadyExistsException;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {
    private CreateHandler handler;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        handler = new CreateHandler();
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void handleRequest_Success() {
        final DescribeLogGroupsResponse describeResponseInitial = DescribeLogGroupsResponse.builder()
                .logGroups(Arrays.asList())
                .build();
        final CreateLogGroupResponse createLogGroupResponse = CreateLogGroupResponse.builder().build();
        final PutRetentionPolicyResponse putRetentionPolicyResponse = PutRetentionPolicyResponse.builder().build();
        final LogGroup logGroup = LogGroup.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .build();

        doReturn(describeResponseInitial, createLogGroupResponse, putRetentionPolicyResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

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
    public void handleRequest_SuccessGeneratedLogGroupName_ModelIsNull() {
        final DescribeLogGroupsResponse describeResponseInitial = DescribeLogGroupsResponse.builder()
            .logGroups(Arrays.asList())
            .build();
        final CreateLogGroupResponse createLogGroupResponse = CreateLogGroupResponse.builder().build();

        doReturn(describeResponseInitial, createLogGroupResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .clientRequestToken("token")
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        // There isn't an easy way to check the generated value of the name
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_SuccessGeneratedLogGroupName() {
        final DescribeLogGroupsResponse describeResponseInitial = DescribeLogGroupsResponse.builder()
                .logGroups(Arrays.asList())
                .build();
        final CreateLogGroupResponse createLogGroupResponse = CreateLogGroupResponse.builder().build();
        final PutRetentionPolicyResponse putRetentionPolicyResponse = PutRetentionPolicyResponse.builder().build();

        doReturn(describeResponseInitial, createLogGroupResponse, putRetentionPolicyResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any()
                );

        final ResourceModel model = ResourceModel.builder()
                .retentionInDays(1)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .logicalResourceIdentifier("id")
                .clientRequestToken("token")
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        // There isn't an easy way to check the generated value of the name
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_FailureAlreadyExists() {
        doThrow(ResourceAlreadyExistsException.class)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any()
                );

        final ResourceModel model = ResourceModel.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnAlreadyExistsException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }
}
