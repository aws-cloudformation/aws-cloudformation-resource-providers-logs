package com.amazonaws.logs.logstream;

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
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final CreateHandler handler = new CreateHandler();

        final DescribeLogStreamsResponse initialDescribeResponse = DescribeLogStreamsResponse.builder()
            .logStreams(Collections.emptyList())
            .build();

        final CreateLogStreamResponse createLogStreamResponse = CreateLogStreamResponse.builder()
            .build();

        final LogStream logStream = LogStream.builder()
            .logStreamName("Stream")
            .build();

        final DescribeLogStreamsResponse describeResponse = DescribeLogStreamsResponse.builder()
            .logStreams(Collections.singletonList(logStream))
            .build();

        doReturn(initialDescribeResponse, createLogStreamResponse, describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .logStreamName("Stream")
            .logGroupName("LogGroup")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_GeneratedName() {
        final CreateHandler handler = new CreateHandler();

        final DescribeLogStreamsResponse initialDescribeResponse = DescribeLogStreamsResponse.builder()
            .logStreams(Collections.emptyList())
            .build();

        final CreateLogStreamResponse createLogStreamResponse = CreateLogStreamResponse.builder()
            .build();

        final LogStream logStream = LogStream.builder()
            .logStreamName("SomeGeneratedStreamName")
            .build();

        final DescribeLogStreamsResponse describeResponse = DescribeLogStreamsResponse.builder()
            .logStreams(Collections.singletonList(logStream))
            .build();

        doReturn(initialDescribeResponse, createLogStreamResponse, describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .logGroupName("LogGroup")
            .build();

        final ResourceModel expectedModel = ResourceModel.builder()
            .logGroupName("LogGroup")
            .logStreamName("SomeGeneratedStreamName")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .clientRequestToken("token")
            .logicalResourceIdentifier("logicalId")
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualToComparingFieldByField(expectedModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_GeneratedNameNoLogicalId() {
        final CreateHandler handler = new CreateHandler();

        final DescribeLogStreamsResponse initialDescribeResponse = DescribeLogStreamsResponse.builder()
            .logStreams(Collections.emptyList())
            .build();

        final CreateLogStreamResponse createLogStreamResponse = CreateLogStreamResponse.builder()
            .build();

        final LogStream logStream = LogStream.builder()
            .logStreamName("SomeGeneratedStreamName")
            .build();

        final DescribeLogStreamsResponse describeResponse = DescribeLogStreamsResponse.builder()
            .logStreams(Collections.singletonList(logStream))
            .build();

        doReturn(initialDescribeResponse, createLogStreamResponse, describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .logGroupName("LogGroup")
            .build();

        final ResourceModel expectedModel = ResourceModel.builder()
            .logGroupName("LogGroup")
            .logStreamName("SomeGeneratedStreamName")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .clientRequestToken("token")
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualToComparingFieldByField(expectedModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_FailureAlreadyExists() {
        final CreateHandler handler = new CreateHandler();

        final LogStream logStream = LogStream.builder()
            .logStreamName("Stream")
            .build();

        final DescribeLogStreamsResponse describeResponse = DescribeLogStreamsResponse.builder()
            .logStreams(Collections.singletonList(logStream))
            .build();

        doReturn(describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .logGroupName("LogGroup")
            .logStreamName("Stream")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(com.amazonaws.cloudformation.exceptions.ResourceAlreadyExistsException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }
}
