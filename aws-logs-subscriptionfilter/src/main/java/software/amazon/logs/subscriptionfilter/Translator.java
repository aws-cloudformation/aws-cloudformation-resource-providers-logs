package software.amazon.logs.subscriptionfilter;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cloudwatchlogs.model.*;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Translator {

  public static BaseHandlerException translateException(final AwsServiceException e) {
    if (e instanceof LimitExceededException) {
      return new CfnServiceLimitExceededException(e);
    }
    if (e instanceof OperationAbortedException) {
      return new CfnResourceConflictException(e);
    }
    if (e instanceof InvalidParameterException) {
      return new CfnInvalidRequestException(e);
    }
    else if (e instanceof ResourceNotFoundException) {
      return new CfnNotFoundException(e);
    }
    else if (e instanceof ServiceUnavailableException) {
      return new CfnServiceInternalErrorException(e);
    }
    return new CfnGeneralServiceException(e);
  }

  static ResourceModel translateSubscriptionFilter
          (final software.amazon.awssdk.services.cloudwatchlogs.model.SubscriptionFilter subscriptionFilter) {
    return ResourceModel.builder()
            .filterName(subscriptionFilter.filterName())
            .destinationArn(subscriptionFilter.destinationArn())
            .filterPattern(subscriptionFilter.filterPattern())
            .logGroupName(subscriptionFilter.logGroupName())
            .roleArn(subscriptionFilter.roleArn())
            .build();
  }

  static software.amazon.awssdk.services.cloudwatchlogs.model.SubscriptionFilter translateToSDK
          (final ResourceModel model) {
    return software.amazon.awssdk.services.cloudwatchlogs.model.SubscriptionFilter.builder()
            .filterName(model.getFilterName())
            .destinationArn(model.getDestinationArn())
            .filterPattern(model.getFilterPattern())
            .logGroupName(model.getLogGroupName())
            .roleArn(model.getRoleArn())
            .build();
  }

  static PutSubscriptionFilterRequest translateToCreateRequest(final ResourceModel model) {
    return PutSubscriptionFilterRequest.builder()
            .filterName(model.getFilterName())
            .destinationArn(model.getDestinationArn())
            .filterPattern(model.getFilterPattern())
            .logGroupName(model.getLogGroupName())
            .roleArn(model.getRoleArn())
            .build();
  }

  static DescribeSubscriptionFiltersRequest translateToReadRequest(final ResourceModel model) {
    return DescribeSubscriptionFiltersRequest.builder()
            .logGroupName(model.getLogGroupName())
            .filterNamePrefix(model.getFilterName())
            .build();
  }

  static ResourceModel translateFromReadResponse(final DescribeSubscriptionFiltersResponse awsResponse) {
    return awsResponse.subscriptionFilters()
            .stream()
            .map(Translator::translateSubscriptionFilter)
            .findFirst()
            .get();
  }

  static List<ResourceModel> translateFromListResponse(final DescribeSubscriptionFiltersResponse awsResponse) {
    return streamOfOrEmpty(awsResponse.subscriptionFilters())
            .map(subscriptionFilter -> ResourceModel.builder()
                    .logGroupName(subscriptionFilter.logGroupName())
                    .filterName(subscriptionFilter.filterName())
                    .destinationArn(subscriptionFilter.destinationArn())
                    .filterPattern(subscriptionFilter.filterPattern())
                    .roleArn(subscriptionFilter.roleArn())
                    .build())
            .collect(Collectors.toList());
  }

  static DeleteSubscriptionFilterRequest translateToDeleteRequest(final ResourceModel model) {
    return DeleteSubscriptionFilterRequest.builder()
            .logGroupName(model.getLogGroupName())
            .filterName(model.getFilterName())
            .build();
  }

  static PutSubscriptionFilterRequest translateToUpdateRequest(final ResourceModel model) {
    return translateToCreateRequest(model);
  }

  static DescribeSubscriptionFiltersRequest translateToListRequest(final ResourceModel model, final String nextToken) {
    return DescribeSubscriptionFiltersRequest.builder()
            .logGroupName(model.getLogGroupName())
            .nextToken(nextToken)
            .limit(50)
            .build();
  }

  private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
    return Optional.ofNullable(collection)
            .map(Collection::stream)
            .orElseGet(Stream::empty);
  }
}