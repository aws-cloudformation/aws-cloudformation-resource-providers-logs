package com.amazonaws.logs.subscriptionfilter;

import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteSubscriptionFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutSubscriptionFilterRequest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Translator {
    static DescribeSubscriptionFiltersRequest requestForRead(final ResourceModel model) {
        return DescribeSubscriptionFiltersRequest.builder()
            .logGroupName(model.getLogGroupName())
            .build();
    }

    static DescribeSubscriptionFiltersRequest requestForList(final ResourceModel model,
                                                             final String nextToken) {
        return DescribeSubscriptionFiltersRequest.builder()
            .nextToken(nextToken)
            .logGroupName(model.getLogGroupName())
            .build();
    }

    static PutSubscriptionFilterRequest requestForPut(final ResourceModel model) {
        return PutSubscriptionFilterRequest.builder()
            .destinationArn(model.getDestinationArn())
            .logGroupName(model.getLogGroupName())
            .filterPattern(model.getFilterPattern())
            .filterName(model.getFilterName())
            .roleArn(model.getRoleArn())
            .build();
    }

    static DeleteSubscriptionFilterRequest requestForDelete(final ResourceModel model) {
        return DeleteSubscriptionFilterRequest.builder()
            .filterName(model.getFilterName())
            .logGroupName(model.getLogGroupName())
            .build();
    }

    static Optional<ResourceModel> translateForRead(final DescribeSubscriptionFiltersResponse response) {
        return streamOfOrEmpty(response.subscriptionFilters())
            .map(filter -> ResourceModel.builder()
                .destinationArn(filter.destinationArn())
                .filterPattern(filter.filterPattern())
                .filterName(filter.filterName())
                .logGroupName(filter.logGroupName())
                .roleArn(filter.roleArn())
                .build())
            .findFirst();
    }

    static List<ResourceModel> translateForList(final DescribeSubscriptionFiltersResponse response) {
        return streamOfOrEmpty(response.subscriptionFilters())
            .map(filter -> ResourceModel.builder()
                .destinationArn(filter.destinationArn())
                .filterPattern(filter.filterPattern())
                .filterName(filter.filterName())
                .logGroupName(filter.logGroupName())
                .roleArn(filter.roleArn())
                .build())
            .collect(Collectors.toList());
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
            .map(Collection::stream)
            .orElseGet(Stream::empty);
    }
}
