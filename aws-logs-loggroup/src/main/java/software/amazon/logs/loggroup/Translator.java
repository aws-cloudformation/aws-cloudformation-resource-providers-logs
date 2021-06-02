package software.amazon.logs.loggroup;

import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ListTagsLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ListTagsLogGroupResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DisassociateKmsKeyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.AssociateKmsKeyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.TagLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.UntagLogGroupRequest;
import software.amazon.awssdk.utils.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class Translator {

    static final String ACCESS_DENIED_ERROR_CODE = "AccessDeniedException";

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

    static CreateLogGroupRequest translateToCreateRequest(final ResourceModel model, final Map<String, String> tags) {
        if(tags == null || tags.size() == 0){
            return CreateLogGroupRequest.builder()
                    .logGroupName(model.getLogGroupName())
                    .kmsKeyId(model.getKmsKeyId())
                    .build();
        }
        else {
            return CreateLogGroupRequest.builder()
                    .logGroupName(model.getLogGroupName())
                    .kmsKeyId(model.getKmsKeyId())
                    .tags(tags)
                    .build();
        }
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

    static DisassociateKmsKeyRequest translateToDisassociateKmsKeyRequest(final ResourceModel model) {
        return DisassociateKmsKeyRequest.builder()
                .logGroupName(model.getLogGroupName())
                .build();
    }

    static AssociateKmsKeyRequest translateToAssociateKmsKeyRequest(final ResourceModel model) {
        return AssociateKmsKeyRequest.builder()
                .logGroupName(model.getLogGroupName())
                .kmsKeyId(model.getKmsKeyId())
                .build();
    }

    static ListTagsLogGroupRequest translateToListTagsLogGroupRequest(final String logGroupName) {
        return ListTagsLogGroupRequest.builder()
                .logGroupName(logGroupName)
                .build();
    }

    static TagLogGroupRequest translateToTagLogGroupRequest(final String logGroupName, final Map<String, String> tags) {
        return TagLogGroupRequest.builder()
                .logGroupName(logGroupName)
                .tags(tags)
                .build();
    }

    static UntagLogGroupRequest translateToUntagLogGroupRequest(final String logGroupName, final List<String> tagKeys) {
        return UntagLogGroupRequest.builder()
                .logGroupName(logGroupName)
                .tags(tagKeys)
                .build();
    }

    static ResourceModel translateForRead(final DescribeLogGroupsResponse response, final ListTagsLogGroupResponse tagsResponse) {
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
        final String kmsKeyId = streamOfOrEmpty(response.logGroups())
                .map(software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup::kmsKeyId)
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
        final Set<Tag> tags = translateSdkToTags(Optional.ofNullable(tagsResponse)
                .map(ListTagsLogGroupResponse::tags)
                .orElse(null));
        return ResourceModel.builder()
                .arn(logGroupArn)
                .logGroupName(logGroupName)
                .retentionInDays(retentionInDays)
                .kmsKeyId(kmsKeyId)
                .tags(tags)
                .build();
    }

    static List<ResourceModel> translateForList(final DescribeLogGroupsResponse response, final Map<String, ListTagsLogGroupResponse> tagResponses) {
        return streamOfOrEmpty(response.logGroups())
                .map(logGroup -> ResourceModel.builder()
                        .arn(logGroup.arn())
                        .logGroupName(logGroup.logGroupName())
                        .retentionInDays(logGroup.retentionInDays())
                        .kmsKeyId(logGroup.kmsKeyId())
                        .tags(translateSdkToTags(tagResponses.get(logGroup.logGroupName()).tags()))
                        .build())
                .collect(Collectors.toList());
    }

    static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
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

    static Map<String, String> translateTagsToSdk(final Set<Tag> tags) {
        if (CollectionUtils.isNullOrEmpty(tags)) {
            return null;
        }
        return tags.stream().collect(Collectors.toMap(Tag::getKey, Tag::getValue));
    }

    static Set<Tag> translateSdkToTags(final Map<String, String> tags) {
        if (CollectionUtils.isNullOrEmpty(tags)) {
            return null;
        }
        return tags.entrySet().stream().map(tag -> new Tag(tag.getKey(), tag.getValue()))
                .collect(Collectors.toSet());
    }
}
