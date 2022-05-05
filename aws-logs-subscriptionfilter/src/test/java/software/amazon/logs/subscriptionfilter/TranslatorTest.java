package software.amazon.logs.subscriptionfilter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;
import software.amazon.cloudformation.exceptions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static software.amazon.logs.subscriptionfilter.Translator.translateException;

@ExtendWith(MockitoExtension.class)
public class TranslatorTest {
    private static final SubscriptionFilter SUBSCRIPTION_FILTER = SubscriptionFilter.builder()
            .logGroupName("LogGroup")
            .destinationArn("DestinationArn")
            .filterPattern("Pattern")
            .roleArn("RoleArn")
            .build();

    private static final ResourceModel RESOURCE_MODEL = ResourceModel.builder()
            .logGroupName("LogGroup")
            .filterName("FilterName")
            .destinationArn("DestinationArn")
            .filterPattern("Pattern")
            .roleArn("RoleArn")
            .build();

    @Test
    public void extractSubscriptionFilters_success() {
        final DescribeSubscriptionFiltersResponse response = DescribeSubscriptionFiltersResponse.builder()
                .subscriptionFilters(Collections.singletonList(SUBSCRIPTION_FILTER))
                .build();

        final List<ResourceModel> expectedModels = Arrays.asList(ResourceModel.builder()
                .logGroupName("LogGroup")
                .destinationArn("DestinationArn")
                .filterPattern("Pattern")
                .roleArn("RoleArn")
                .build());

        assertThat(Translator.translateFromListResponse(response)).isEqualTo(expectedModels);
    }

    @Test
    public void extractSubscriptionFilters_API_removesEmptyFilterPattern() {
        final DescribeSubscriptionFiltersResponse response = DescribeSubscriptionFiltersResponse.builder()
                .subscriptionFilters(Collections.singletonList(SUBSCRIPTION_FILTER.toBuilder()
                        .filterPattern(null)
                        .build()))
                .build();

        final List<ResourceModel> expectedModels = Arrays.asList(ResourceModel.builder()
                .logGroupName("LogGroup")
                .destinationArn("DestinationArn")
                .roleArn("RoleArn")
                .build());

        assertThat(Translator.translateFromListResponse(response)).isEqualTo(expectedModels);
    }

    @Test
    public void extractSubscriptionFilters_noFilters() {
        final DescribeSubscriptionFiltersResponse response = DescribeSubscriptionFiltersResponse.builder()
                .subscriptionFilters(Collections.emptyList())
                .build();
        final List<ResourceModel> expectedModels = Collections.emptyList();

        assertThat(Translator.translateFromListResponse(response)).isEqualTo(expectedModels);
    }

    @Test
    public void translateToDeleteRequest() {
        final DeleteSubscriptionFilterRequest expectedRequest = DeleteSubscriptionFilterRequest.builder()
                .filterName("FilterName")
                .logGroupName("LogGroup")
                .build();

        final DeleteSubscriptionFilterRequest actualRequest = Translator.translateToDeleteRequest(RESOURCE_MODEL);

        assertThat(actualRequest).isEqualToComparingFieldByField(expectedRequest);
    }

    @Test
    public void translateToPutRequest() {
        final PutSubscriptionFilterRequest expectedRequest = PutSubscriptionFilterRequest.builder()
                .logGroupName("LogGroup")
                .filterName("FilterName")
                .destinationArn("DestinationArn")
                .filterPattern("Pattern")
                .roleArn("RoleArn")
                .build();

        final DeleteSubscriptionFilterRequest actualRequest = Translator.translateToDeleteRequest(RESOURCE_MODEL);

        assertThat(actualRequest).isEqualToComparingFieldByField(expectedRequest);
    }

    @Test
    public void translateToReadRequest() {
        final DescribeSubscriptionFiltersRequest expectedRequest = DescribeSubscriptionFiltersRequest.builder()
                .logGroupName("LogGroup")
                .filterNamePrefix("FilterName")
                .build();

        final DescribeSubscriptionFiltersRequest actualRequest = Translator.translateToReadRequest(RESOURCE_MODEL);

        assertThat(actualRequest).isEqualToComparingFieldByField(expectedRequest);
    }

    @Test
    public void translateToListRequest() {
        final DescribeSubscriptionFiltersRequest expectedRequest = DescribeSubscriptionFiltersRequest.builder()
                .logGroupName("LogGroup")
                .limit(50)
                .nextToken("token")
                .build();

        final DescribeSubscriptionFiltersRequest actualRequest = Translator.translateToListRequest(RESOURCE_MODEL, "token");

        assertThat(actualRequest).isEqualToComparingFieldByField(expectedRequest);
    }

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
