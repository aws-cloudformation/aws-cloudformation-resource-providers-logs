package software.amazon.logs.loggroup;

import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutRetentionPolicyRequest;
import software.amazon.logs.loggroup.ResourceModel;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Translator {
    private Translator() {}

    static DescribeLogGroupsRequest translateToReadRequest(final ResourceModel model) {
        return DescribeLogGroupsRequest.builder()
                .logGroupNamePrefix(model.getLogGroupName())
                .build();
    }

    static DescribeLogGroupsRequest translateToListRequest(final String nextToken) {
        return DescribeLogGroupsRequest.builder()
                .limit(50)
                .nextToken(nextToken)
                .build();
    }

    static DeleteLogGroupRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteLogGroupRequest.builder()
                .logGroupName(model.getLogGroupName())
                .build();
    }

    static CreateLogGroupRequest translateToCreateRequest(final ResourceModel model) {
        return CreateLogGroupRequest.builder()
                .logGroupName(model.getLogGroupName())
                .build();
    }

    static PutRetentionPolicyRequest translateToPutRetentionPolicyRequest(final ResourceModel model) {
        return PutRetentionPolicyRequest.builder()
                .logGroupName(model.getLogGroupName())
                .retentionInDays(model.getRetentionInDays())
                .build();
    }

    static DeleteRetentionPolicyRequest translateToDeleteRetentionPolicyRequest(final ResourceModel model) {
        return DeleteRetentionPolicyRequest.builder()
            .logGroupName(model.getLogGroupName())
            .build();
    }

    static ResourceModel translateForRead(final DescribeLogGroupsResponse response) {
        final String logGroupName = streamOfOrEmpty(response.logGroups())
                .map(software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup::logGroupName)
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
        final String logGroupArn = streamOfOrEmpty(response.logGroups())
            .map(software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup::arn)
            .filter(Objects::nonNull)
            .findAny()
            .orElse(null);
        final Integer retentionInDays = streamOfOrEmpty(response.logGroups())
                .map(software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup::retentionInDays)
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
        return ResourceModel.builder()
                .arn(logGroupArn)
                .logGroupName(logGroupName)
                .retentionInDays(retentionInDays)
                .build();
    }

    static List<ResourceModel> translateForList(final DescribeLogGroupsResponse response) {
        return streamOfOrEmpty(response.logGroups())
                .map(logGroup -> ResourceModel.builder()
                        .arn(logGroup.arn())
                        .logGroupName(logGroup.logGroupName())
                        .retentionInDays(logGroup.retentionInDays())
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
