package com.amazonaws.logs.subscriptionfilter;

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
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutSubscriptionFilterResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.SubscriptionFilter;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

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
    public void handleRequest_SimpleSuccess() {
        final DescribeSubscriptionFiltersResponse describeResponse = DescribeSubscriptionFiltersResponse.builder()
            .subscriptionFilters(Collections.emptyList())
            .build();

        final PutSubscriptionFilterResponse putResponse = PutSubscriptionFilterResponse.builder().build();

        final SubscriptionFilter filter = SubscriptionFilter.builder()
            .destinationArn("DestinationArn")
            .logGroupName("LogGroupName")
            .roleArn("RoleArn")
            .filterPattern("FilterPattern")
            .build();
        final DescribeSubscriptionFiltersResponse postPutDescribeResponse = DescribeSubscriptionFiltersResponse.builder()
            .subscriptionFilters(Collections.singletonList(filter))
            .build();

        doReturn(describeResponse, putResponse, postPutDescribeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .destinationArn("DestinationArn")
            .logGroupName("LogGroupName")
            .roleArn("RoleArn")
            .filterPattern("FilterPattern")
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
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_Success_WithResourceId() {
        final DescribeSubscriptionFiltersResponse describeResponse = DescribeSubscriptionFiltersResponse.builder()
            .subscriptionFilters(Collections.emptyList())
            .build();

        final PutSubscriptionFilterResponse putResponse = PutSubscriptionFilterResponse.builder().build();

        final SubscriptionFilter filter = SubscriptionFilter.builder()
            .destinationArn("DestinationArn")
            .logGroupName("LogGroupName")
            .roleArn("RoleArn")
            .filterPattern("FilterPattern")
            .build();
        final DescribeSubscriptionFiltersResponse postPutDescribeResponse = DescribeSubscriptionFiltersResponse.builder()
            .subscriptionFilters(Collections.singletonList(filter))
            .build();

        doReturn(describeResponse, putResponse, postPutDescribeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .destinationArn("DestinationArn")
            .logGroupName("LogGroupName")
            .roleArn("RoleArn")
            .filterPattern("FilterPattern")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .logicalResourceIdentifier("ResourceId")
            .clientRequestToken("token")
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_AlreadyExists() {
        final SubscriptionFilter filter = SubscriptionFilter.builder()
            .destinationArn("DestinationArn")
            .logGroupName("LogGroupName")
            .roleArn("RoleArn")
            .filterPattern("FilterPattern")
            .build();
        final DescribeSubscriptionFiltersResponse describeResponse = DescribeSubscriptionFiltersResponse.builder()
            .subscriptionFilters(Collections.singletonList(filter))
            .build();

        doReturn(describeResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .logGroupName("LogGroupName")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .clientRequestToken("token")
            .desiredResourceState(model)
            .build();

        assertThrows(com.amazonaws.cloudformation.exceptions.ResourceAlreadyExistsException.class,
            () -> handler.handleRequest(proxy, request, null, logger)
        );
    }
}
