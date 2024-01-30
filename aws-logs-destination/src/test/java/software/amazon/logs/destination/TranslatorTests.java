package software.amazon.logs.destination;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteDestinationRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;

class TranslatorTests extends AbstractTestBase {

    private ResourceModel resourceModel;

    private static final String TOKEN = "token";

    @BeforeEach
    public void setup() {
        resourceModel = getTestResourceModel();
    }

    @Test
    void translateToCreateRequest_Should_ReturnSuccess() {
        PutDestinationRequest putDestinationRequest = PutDestinationRequest
            .builder()
            .destinationName(TEST_DESTINATION_INPUT)
            .roleArn(TEST_ROLE_ARN)
            .targetArn(TEST_TARGET_ARN)
            .build();
        assertThat(Translator.translateToPutDestinationRequest(resourceModel)).isEqualToComparingFieldByField(putDestinationRequest);
    }

    @Test
    void translateToPutDestinationPolicyRequest_Should_ReturnSuccess() {
        PutDestinationPolicyRequest putDestinationPolicyRequest = PutDestinationPolicyRequest
            .builder()
            .destinationName(TEST_DESTINATION_INPUT)
            .accessPolicy(TEST_ACCESS_POLICY)
            .build();
        assertThat(Translator.translateToPutDestinationPolicyRequest(resourceModel))
            .isEqualToComparingFieldByField(putDestinationPolicyRequest);
    }

    @Test
    void translateToReadRequest_Should_ReturnSuccess() {
        DescribeDestinationsRequest describeDestinationsRequest = DescribeDestinationsRequest
            .builder()
            .destinationNamePrefix(TEST_DESTINATION_INPUT)
            .build();
        assertThat(Translator.translateToReadRequest(resourceModel)).isEqualToComparingFieldByField(describeDestinationsRequest);
    }

    @Test
    void translateToDeleteRequest_Should_ReturnSuccess() {
        DeleteDestinationRequest deleteDestinationRequest = DeleteDestinationRequest
            .builder()
            .destinationName(TEST_DESTINATION_INPUT)
            .build();
        assertThat(Translator.translateToDeleteRequest(resourceModel)).isEqualToComparingFieldByField(deleteDestinationRequest);
    }

    @Test
    void translateToReadResponse_Should_ReturnSuccess() {
        DescribeDestinationsResponse describeDestinationsResponse = DescribeDestinationsResponse
            .builder()
            .destinations(getTestDestination())
            .build();
        assertThat(Translator.translateFromReadResponse(describeDestinationsResponse)).isEqualToComparingFieldByField(resourceModel);
    }

    @Test
    void translateToReadResponse_Should_ReturnNull_When_DestinationIsNull() {
        DescribeDestinationsResponse describeDestinationsResponse = DescribeDestinationsResponse.builder().build();
        assertThat(Translator.translateFromReadResponse(describeDestinationsResponse)).isNull();
    }

    @Test
    void translateToReadResponse_Should_ReturnNull_When_DestinationIsEmpty() {
        DescribeDestinationsResponse describeDestinationsResponse = DescribeDestinationsResponse
            .builder()
            .destinations(Collections.emptyList())
            .build();
        assertThat(Translator.translateFromReadResponse(describeDestinationsResponse)).isNull();
    }

    @Test
    void translateFromListResponse_Should_ReturnSuccess() {
        final DescribeDestinationsResponse response = DescribeDestinationsResponse
            .builder()
            .destinations(Collections.singletonList(getTestDestination()))
            .nextToken(TOKEN)
            .build();
        final List<ResourceModel> expectedModels = Collections.singletonList(getTestResourceModel());
        assertThat(Translator.translateFromListResponse(response)).isEqualTo(expectedModels);
    }

    @Test
    void translateFromListResponse_Should_ReturnEmptyList_WhenDestinationsIsEmpty() {
        final DescribeDestinationsResponse response = DescribeDestinationsResponse
            .builder()
            .destinations(Collections.emptyList())
            .nextToken(TOKEN)
            .build();
        final List<ResourceModel> expectedModels = Collections.emptyList();
        assertThat(Translator.translateFromListResponse(response)).isEqualTo(expectedModels);
    }

    @Test
    void translateFromListResponse_Should_ReturnEmptyList_WhenDestinationsIsNull() {
        final DescribeDestinationsResponse response = DescribeDestinationsResponse.builder().nextToken(TOKEN).build();
        final List<ResourceModel> expectedModels = Collections.emptyList();
        assertThat(Translator.translateFromListResponse(response)).isEqualTo(expectedModels);
    }

    @Test
    void translateToListRequest_Should_ReturnSuccess() {
        final DescribeDestinationsRequest request = DescribeDestinationsRequest.builder().build();
        assertThat(Translator.translateToListRequest(ResourceModel.builder().build())).isEqualToComparingFieldByField(request);
    }

    @Test
    void translateException_Should_ThrowCfnInvalidRequestException() {
        InvalidParameterException exception = InvalidParameterException.builder().build();
        assertThat(Translator.translateException(exception)).isInstanceOf(CfnInvalidRequestException.class);
    }

    @Test
    void translateException_Should_ThrowCfnServiceInternalErrorException() {
        ServiceUnavailableException exception = ServiceUnavailableException.builder().build();
        assertThat(Translator.translateException(exception)).isInstanceOf(CfnServiceInternalErrorException.class);
    }

    @Test
    void translateException_Should_ThrowCfnNotFoundException() {
        ResourceNotFoundException exception = ResourceNotFoundException.builder().build();
        assertThat(Translator.translateException(exception)).isInstanceOf(CfnNotFoundException.class);
    }

    @Test
    void translateException_Should_ThrowCfnGeneralServiceException() {
        CloudWatchLogsException exception = (CloudWatchLogsException) CloudWatchLogsException.builder().build();
        assertThat(Translator.translateException(exception)).isInstanceOf(CfnGeneralServiceException.class);
    }
}
