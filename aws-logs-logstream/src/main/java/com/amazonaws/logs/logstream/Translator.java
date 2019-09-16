package com.amazonaws.logs.logstream;

import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Translator {
    private Translator() {}

    static DescribeLogStreamsRequest translateToReadRequest(final ResourceModel model) {
        return DescribeLogStreamsRequest.builder()
            .logGroupName(model.getLogGroupName())
            .logStreamNamePrefix(model.getLogStreamName())
            .build();
    }

    static DescribeLogStreamsRequest translateToListRequest(final ResourceModel model,
                                                            final String nextToken) {
        return DescribeLogStreamsRequest.builder()
            .limit(50)
            .logGroupName(model.getLogGroupName())
            .nextToken(nextToken)
            .build();
    }

    static CreateLogStreamRequest translateToCreateRequest(final ResourceModel model) {
        return CreateLogStreamRequest.builder()
            .logGroupName(model.getLogGroupName())
            .logStreamName(model.getLogStreamName())
            .build();
    }

    static DeleteLogStreamRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteLogStreamRequest.builder()
            .logGroupName(model.getLogGroupName())
            .logStreamName(model.getLogStreamName())
            .build();
    }

    static ResourceModel translateForRead(final DescribeLogStreamsResponse result,
                                          final String logGroupName) {
        final String logStreamName = streamOfOrEmpty(result.logStreams())
            .map(software.amazon.awssdk.services.cloudwatchlogs.model.LogStream::logStreamName)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);
        return ResourceModel.builder()
            .logGroupName(logGroupName)
            .logStreamName(logStreamName)
            .build();
    }

    static List<ResourceModel> translateForList(final DescribeLogStreamsResponse result,
                                                final String logGroupName) {
        return streamOfOrEmpty(result.logStreams())
            .map(logStream -> ResourceModel.builder()
                .logStreamName(logStream.logStreamName())
                .logGroupName(logGroupName)
                .build())
            .collect(Collectors.toList());
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
            .map(Collection::stream)
            .orElseGet(Stream::empty);
    }

    static String buildResourceAlreadyExistsErrorMessage(final String resourceIdentifier) {
        return String.format("Resource of type '%s' with identifier '%s' already exists.",
            ResourceModel.TYPE_NAME,
            resourceIdentifier);
    }

    static String buildResourceDoesNotExistErrorMessage(final String resourceIdentifier) {
        return String.format("Resource of type '%s' with identifier '%s' was not found.",
            ResourceModel.TYPE_NAME,
            resourceIdentifier);
    }
}
