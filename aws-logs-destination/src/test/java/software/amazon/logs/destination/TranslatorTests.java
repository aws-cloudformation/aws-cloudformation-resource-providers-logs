package software.amazon.logs.destination;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteDestinationRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TranslatorTests extends AbstractTestBase {

    private ResourceModel resourceModel;

    @BeforeEach
    public void setup() {

        resourceModel = getTestResourceModel();
    }

    @Test
    public void translateToCreateRequest_Should_ReturnSuccess() {

        PutDestinationRequest putDestinationRequest = PutDestinationRequest.builder()
                .destinationName(TEST_DESTINATION_INPUT)
                .roleArn(TEST_ROLE_ARN)
                .targetArn(TEST_TARGET_ARN)
                .build();
        Assertions.assertThat(Translator.translateToCreateRequest(resourceModel))
                .isEqualToComparingFieldByField(putDestinationRequest);
    }

    @Test
    public void translateToPutDestinationPolicyRequest_Should_ReturnSuccess() {

        PutDestinationPolicyRequest putDestinationPolicyRequest = PutDestinationPolicyRequest.builder()
                .destinationName(TEST_DESTINATION_INPUT)
                .accessPolicy(TEST_ACCESS_POLICY)
                .build();
        Assertions.assertThat(Translator.translateToPutDestinationPolicyRequest(resourceModel))
                .isEqualToComparingFieldByField(putDestinationPolicyRequest);
    }

    @Test
    public void translateToReadRequest_Should_ReturnSuccess() {

        DescribeDestinationsRequest describeDestinationsRequest =
                DescribeDestinationsRequest.builder().destinationNamePrefix(TEST_DESTINATION_INPUT).build();
        Assertions.assertThat(Translator.translateToReadRequest(resourceModel))
                .isEqualToComparingFieldByField(describeDestinationsRequest);
    }

    @Test
    public void translateToDeleteRequest_Should_ReturnSuccess() {

        DeleteDestinationRequest deleteDestinationRequest =
                DeleteDestinationRequest.builder().destinationName(TEST_DESTINATION_INPUT).build();
        Assertions.assertThat(Translator.translateToDeleteRequest(resourceModel))
                .isEqualToComparingFieldByField(deleteDestinationRequest);
    }

    @Test
    public void translateToReadResponse_Should_ReturnSuccess() {

        DescribeDestinationsResponse describeDestinationsResponse =
                DescribeDestinationsResponse.builder().destinations(getTestDestination()).build();
        Assertions.assertThat(Translator.translateFromReadResponse(describeDestinationsResponse))
                .isEqualToComparingFieldByField(resourceModel);
    }

    @Test
    public void translateToReadResponse_Should_ReturnNull_When_DestinationIsNull() {

        DescribeDestinationsResponse describeDestinationsResponse = DescribeDestinationsResponse.builder().build();
        Assertions.assertThat(Translator.translateFromReadResponse(describeDestinationsResponse)).isNull();
    }

    @Test
    public void translateToReadResponse_Should_ReturnNull_When_DestinationIsEmpty() {

        DescribeDestinationsResponse describeDestinationsResponse =
                DescribeDestinationsResponse.builder().destinations(Collections.emptyList()).build();
        Assertions.assertThat(Translator.translateFromReadResponse(describeDestinationsResponse)).isNull();
    }

    @Test
    public void translateFromListResponse_Should_ReturnSuccess() {

        final DescribeDestinationsResponse response = DescribeDestinationsResponse.builder()
                .destinations(Collections.singletonList(getTestDestination()))
                .nextToken("token")
                .build();
        final List<ResourceModel> expectedModels = Arrays.asList(getTestResourceModel());
        Assertions.assertThat(Translator.translateFromListResponse(response)).isEqualTo(expectedModels);
    }

    @Test
    public void translateFromListResponse_Should_ReturnEmptyList_WhenDestinationsIsEmpty() {

        final DescribeDestinationsResponse response = DescribeDestinationsResponse.builder()
                .destinations(Collections.emptyList())
                .nextToken("token")
                .build();
        final List<ResourceModel> expectedModels = Collections.emptyList();
        Assertions.assertThat(Translator.translateFromListResponse(response)).isEqualTo(expectedModels);
    }

    @Test
    public void translateFromListResponse_Should_ReturnEmptyList_WhenDestinationsIsNull() {

        final DescribeDestinationsResponse response = DescribeDestinationsResponse.builder()
                .nextToken("token")
                .build();
        final List<ResourceModel> expectedModels = Collections.emptyList();
        Assertions.assertThat(Translator.translateFromListResponse(response)).isEqualTo(expectedModels);
    }

    @Test
    public void translateToListRequest_Should_ReturnSuccess() {

        final DescribeDestinationsRequest request =
                DescribeDestinationsRequest.builder().nextToken("token").limit(50).build();
        Assertions.assertThat(Translator.translateToListRequest("token")).isEqualToComparingFieldByField(request);
    }

}
