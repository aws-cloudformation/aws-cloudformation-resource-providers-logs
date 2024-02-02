package software.amazon.logs.subscriptionfilter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.model.AccessDeniedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteSubscriptionFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.LimitExceededException;
import software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutSubscriptionFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;

public class Translator {

    private static final int RESPONSE_LIMIT = 50;

    public static BaseHandlerException translateException(final AwsServiceException e) {
        if (e instanceof InvalidParameterException) {
            return new CfnInvalidRequestException(String.format("%s. %s", ResourceModel.TYPE_NAME, e.getMessage()), e);
        } else if (e instanceof LimitExceededException) {
            return new CfnServiceLimitExceededException(e);
        } else if (e instanceof OperationAbortedException) {
            return new CfnResourceConflictException(e);
        } else if (e instanceof ResourceNotFoundException) {
            return new CfnNotFoundException(e);
        } else if (e instanceof ServiceUnavailableException) {
            return new CfnServiceInternalErrorException(e);
        } else if (e instanceof AccessDeniedException) {
            return new CfnAccessDeniedException(e);
        }
        return new CfnGeneralServiceException(e);
    }

    static ResourceModel translateSubscriptionFilter(
        final software.amazon.awssdk.services.cloudwatchlogs.model.SubscriptionFilter subscriptionFilter
    ) {
        return ResourceModel
            .builder()
            .filterName(subscriptionFilter.filterName())
            .destinationArn(subscriptionFilter.destinationArn())
            .filterPattern(subscriptionFilter.filterPattern())
            .logGroupName(subscriptionFilter.logGroupName())
            .roleArn(subscriptionFilter.roleArn())
            .distribution(subscriptionFilter.distributionAsString())
            .build();
    }

    static software.amazon.awssdk.services.cloudwatchlogs.model.SubscriptionFilter translateToSDK(final ResourceModel model) {
        return software.amazon.awssdk.services.cloudwatchlogs.model.SubscriptionFilter
            .builder()
            .filterName(model.getFilterName())
            .destinationArn(model.getDestinationArn())
            .filterPattern(model.getFilterPattern())
            .logGroupName(model.getLogGroupName())
            .roleArn(model.getRoleArn())
            .distribution(model.getDistribution())
            .build();
    }

    static PutSubscriptionFilterRequest translateToCreateRequest(final ResourceModel model) {
        return PutSubscriptionFilterRequest
            .builder()
            .filterName(model.getFilterName())
            .destinationArn(model.getDestinationArn())
            .filterPattern(model.getFilterPattern())
            .logGroupName(model.getLogGroupName())
            .roleArn(model.getRoleArn())
            .distribution(model.getDistribution())
            .build();
    }

    static DescribeSubscriptionFiltersRequest translateToReadRequest(final ResourceModel model) {
        return DescribeSubscriptionFiltersRequest
            .builder()
            .logGroupName(model.getLogGroupName())
            .filterNamePrefix(model.getFilterName())
            .build();
    }

    static ResourceModel translateFromReadResponse(final DescribeSubscriptionFiltersResponse awsResponse) {
        return awsResponse.subscriptionFilters().stream().map(Translator::translateSubscriptionFilter).findFirst().get();
    }

    static List<ResourceModel> translateFromListResponse(final DescribeSubscriptionFiltersResponse awsResponse) {
        return streamOfOrEmpty(awsResponse.subscriptionFilters())
            .map(subscriptionFilter ->
                ResourceModel
                    .builder()
                    .logGroupName(subscriptionFilter.logGroupName())
                    .filterName(subscriptionFilter.filterName())
                    .destinationArn(subscriptionFilter.destinationArn())
                    .filterPattern(subscriptionFilter.filterPattern())
                    .roleArn(subscriptionFilter.roleArn())
                    .distribution(subscriptionFilter.distributionAsString())
                    .build()
            )
            .collect(Collectors.toList());
    }

    static DeleteSubscriptionFilterRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteSubscriptionFilterRequest.builder().logGroupName(model.getLogGroupName()).filterName(model.getFilterName()).build();
    }

    static PutSubscriptionFilterRequest translateToUpdateRequest(final ResourceModel model) {
        return translateToCreateRequest(model);
    }

    static DescribeSubscriptionFiltersRequest translateToListRequest(final ResourceModel model, final String nextToken) {
        return DescribeSubscriptionFiltersRequest
            .builder()
            .logGroupName(model.getLogGroupName())
            .nextToken(nextToken)
            .limit(RESPONSE_LIMIT)
            .build();
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection).map(Collection::stream).orElseGet(Stream::empty);
    }
}
