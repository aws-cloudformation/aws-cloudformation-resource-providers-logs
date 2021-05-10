package software.amazon.logs.loggroup;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ListTagsLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ListTagsLogGroupResponse;
import software.amazon.cloudformation.exceptions.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {
    ReadHandler handler;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        handler = new ReadHandler();
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void handleRequest_Success() {
        final LogGroup logGroup = LogGroup.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .build();
        final Set<Tag> tags = new HashSet<>(Arrays.asList(
                Tag.builder().key("key-1").value("value-1").build(),
                Tag.builder().key("key-2").value("value-2").build()
        ));
        final DescribeLogGroupsResponse describeResponse = DescribeLogGroupsResponse.builder()
                .logGroups(Collections.singletonList(logGroup))
                .build();
        final ListTagsLogGroupResponse tagsResponse = ListTagsLogGroupResponse.builder()
                .tags(Translator.translateTagsToSdk(tags))
                .build();

        doReturn(describeResponse, tagsResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .tags(tags)
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
        assertThat(response.getResourceModel()).isEqualToComparingOnlyGivenFields(logGroup);
        assertThat(response.getResourceModel().getTags()).isEqualTo(tags);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_FailureNotFound_EmptyLogGroupResponse() {
        final DescribeLogGroupsResponse describeResponse = DescribeLogGroupsResponse.builder()
                .logGroups(Collections.emptyList())
                .build();
        final ListTagsLogGroupResponse tagsResponse = ListTagsLogGroupResponse.builder()
                .tags(Collections.emptyMap())
                .build();

        doReturn(describeResponse, tagsResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any()
                );

        final ResourceModel model = ResourceModel.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(ResourceNotFoundException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_FailureNotFound_WithException() {
        doThrow(software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException.class)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel model = ResourceModel.builder()
            .logGroupName("LogGroup")
            .retentionInDays(1)
            .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(ResourceNotFoundException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_FailureNotFound_NullLogGroupInput() {
        final ResourceModel model = ResourceModel.builder()
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        assertThrows(ResourceNotFoundException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_FailureNotFound_NullModel() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .build();

        assertThrows(ResourceNotFoundException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_FailureListTagsAccessDenied_NoException() {
        final LogGroup logGroup = LogGroup.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .build();
        final DescribeLogGroupsResponse describeResponse = DescribeLogGroupsResponse.builder()
                .logGroups(Collections.singletonList(logGroup))
                .build();

        doReturn(describeResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.isA(DescribeLogGroupsRequest.class),
                        ArgumentMatchers.any()
                );
        final AwsServiceException exception = CloudWatchLogsException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode("AccessDeniedException")
                        .build())
                .build();
        doThrow(exception)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.isA(ListTagsLogGroupRequest.class),
                        ArgumentMatchers.any()
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
        assertThat(response.getResourceModel()).isEqualToComparingOnlyGivenFields(logGroup);
        assertThat(response.getResourceModel().getTags()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_FailureListTags_WithException() {
        final LogGroup logGroup = LogGroup.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .build();
        final DescribeLogGroupsResponse describeResponse = DescribeLogGroupsResponse.builder()
                .logGroups(Collections.singletonList(logGroup))
                .build();

        doReturn(describeResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.isA(DescribeLogGroupsRequest.class),
                        ArgumentMatchers.any()
                );
        final AwsServiceException exception = CloudWatchLogsException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode("InternalFailure")
                        .build())
                .build();
        doThrow(exception)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.isA(ListTagsLogGroupRequest.class),
                        ArgumentMatchers.any()
                );

        final ResourceModel model = ResourceModel.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(AwsServiceException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }
}
