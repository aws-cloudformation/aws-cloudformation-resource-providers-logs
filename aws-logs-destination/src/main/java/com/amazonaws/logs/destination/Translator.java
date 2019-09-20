package com.amazonaws.logs.destination;

import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteDestinationRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.Destination;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationPolicyResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutDestinationResponse;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Translator {
    private Translator() {}

    static DescribeDestinationsRequest translateToReadRequest(final ResourceModel model) {
        return DescribeDestinationsRequest.builder()
            .destinationNamePrefix(model.getDestinationName())
            .build();
    }

    static DescribeDestinationsRequest translateToListRequest(final String nextToken) {
        return DescribeDestinationsRequest.builder()
            .limit(50)
            .nextToken(nextToken)
            .build();
    }

    static PutDestinationRequest translateToPutRequest(final ResourceModel model) {
        return PutDestinationRequest.builder()
            .destinationName(model.getDestinationName())
            .roleArn(model.getRoleArn())
            .targetArn(model.getTargetArn())
            .build();
    }

    static DeleteDestinationRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteDestinationRequest.builder()
            .destinationName(model.getDestinationName())
            .build();
    }

    static PutDestinationPolicyRequest translateToPutPolicyRequest(final ResourceModel model) {
        return PutDestinationPolicyRequest.builder()
            .accessPolicy(model.getDestinationPolicy())
            .destinationName(model.getDestinationName())
            .build();
    }

    static ResourceModel translate(final PutDestinationResponse putResponse,
                                   final PutDestinationPolicyResponse putPolicyResponse) {
        final Destination destination = putResponse.destination();
        return ResourceModel.builder()
            .destinationPolicy(putPolicyResponse.toString())
            .destinationName(destination.destinationName())
            .targetArn(destination.targetArn())
            .roleArn(destination.roleArn())
            .build();
    }

    static ResourceModel translateForRead(final DescribeDestinationsResponse result) {
        return streamOfOrEmpty(result.destinations())
            .findAny()
            .map(destination -> ResourceModel.builder()
                .destinationName(destination.destinationName())
                .destinationPolicy(destination.accessPolicy())
                .roleArn(destination.roleArn())
                .targetArn(destination.targetArn())
                .build())
            .orElse(null);
    }

    static List<ResourceModel> translateForList(final DescribeDestinationsResponse result) {
        return streamOfOrEmpty(result.destinations())
            .map(destination -> ResourceModel.builder()
                .destinationName(destination.destinationName())
                .destinationPolicy(destination.accessPolicy())
                .roleArn(destination.roleArn())
                .targetArn(destination.targetArn())
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
