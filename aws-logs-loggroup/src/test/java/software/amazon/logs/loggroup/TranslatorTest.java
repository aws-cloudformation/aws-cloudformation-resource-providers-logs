package software.amazon.logs.loggroup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ListTagsLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ListTagsLogGroupResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DisassociateKmsKeyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.AssociateKmsKeyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.TagLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.UntagLogGroupRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class TranslatorTest {
    private static final Set<Tag> SET_TAGS = new HashSet<>(Arrays.asList(
            Tag.builder().key("key-1").value("value-1").build(),
            Tag.builder().key("key-2").value("value-2").build()
    ));

    private static final Map<String, String> MAP_TAGS = new HashMap<String, String>() {{
        put("key-1", "value-1");
        put("key-2", "value-2");
    }};

    private static final ResourceModel RESOURCE_MODEL = ResourceModel.builder()
            .retentionInDays(1)
            .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .logGroupName("LogGroup")
            .tags(SET_TAGS)
            .build();

    @Test
    public void testTranslateToRead() {
        final DescribeLogGroupsRequest request = DescribeLogGroupsRequest.builder()
            .logGroupNamePrefix(RESOURCE_MODEL.getLogGroupName())
            .build();
        assertThat(Translator.translateToReadRequest(RESOURCE_MODEL, null)).isEqualToComparingFieldByField(request);
    }

    @Test
    public void testTranslateToList() {
        final DescribeLogGroupsRequest request = DescribeLogGroupsRequest.builder()
            .nextToken("token")
            .limit(50)
            .build();
        assertThat(Translator.translateToListRequest("token")).isEqualToComparingFieldByField(request);
    }

    @Test
    public void testTranslateToDelete() {
        final DeleteLogGroupRequest request = DeleteLogGroupRequest.builder()
            .logGroupName(RESOURCE_MODEL.getLogGroupName())
            .build();
        assertThat(Translator.translateToDeleteRequest(RESOURCE_MODEL)).isEqualToComparingFieldByField(request);
    }

    @Test
    public void testTranslateToCreate() {
        final CreateLogGroupRequest request = CreateLogGroupRequest.builder()
                .logGroupName(RESOURCE_MODEL.getLogGroupName())
                .kmsKeyId(RESOURCE_MODEL.getKmsKeyId())
                .tags(MAP_TAGS)
                .build();
        assertThat(Translator.translateToCreateRequest(RESOURCE_MODEL, MAP_TAGS)).isEqualToComparingFieldByField(request);
    }

    @Test
    public void testTranslateToPutRetentionPolicyRequest() {
        final PutRetentionPolicyRequest request = PutRetentionPolicyRequest.builder()
            .logGroupName(RESOURCE_MODEL.getLogGroupName())
            .retentionInDays(RESOURCE_MODEL.getRetentionInDays())
            .build();

        assertThat(Translator.translateToPutRetentionPolicyRequest(RESOURCE_MODEL))
            .isEqualToComparingFieldByField(request);
    }

    @Test
    public void testTranslateToDeleteRetentionPolicyRequest() {
        final DeleteRetentionPolicyRequest request = DeleteRetentionPolicyRequest.builder()
            .logGroupName(RESOURCE_MODEL.getLogGroupName())
            .build();

        assertThat(Translator.translateToDeleteRetentionPolicyRequest(RESOURCE_MODEL))
            .isEqualToComparingFieldByField(request);
    }

    @Test
    public void testTranslateToDisassociateKmsKeyRequest() {
        final DisassociateKmsKeyRequest request = DisassociateKmsKeyRequest.builder()
            .logGroupName(RESOURCE_MODEL.getLogGroupName())
            .build();

        assertThat(Translator.translateToDisassociateKmsKeyRequest(RESOURCE_MODEL))
            .isEqualToComparingFieldByField(request);
    }

    @Test
    public void testTranslateToAssociateKmsKeyRequest() {
        final AssociateKmsKeyRequest request = AssociateKmsKeyRequest.builder()
            .logGroupName(RESOURCE_MODEL.getLogGroupName())
            .kmsKeyId(RESOURCE_MODEL.getKmsKeyId())
            .build();

        assertThat(Translator.translateToAssociateKmsKeyRequest(RESOURCE_MODEL))
            .isEqualToComparingFieldByField(request);
    }

    @Test
    public void testTranslateToListTagsLogGroupRequest() {
        final ListTagsLogGroupRequest request = ListTagsLogGroupRequest.builder()
                .logGroupName(RESOURCE_MODEL.getLogGroupName())
                .build();

        assertThat(Translator.translateToListTagsLogGroupRequest(RESOURCE_MODEL.getLogGroupName()))
                .isEqualToComparingFieldByField(request);
    }

    @Test
    public void testTranslateToTagLogGroupRequest() {
        final TagLogGroupRequest request = TagLogGroupRequest.builder()
                .logGroupName(RESOURCE_MODEL.getLogGroupName())
                .tags(MAP_TAGS)
                .build();

        assertThat(Translator.translateToTagLogGroupRequest(RESOURCE_MODEL.getLogGroupName(), MAP_TAGS))
                .isEqualToComparingFieldByField(request);
    }

    @Test
    public void testTranslateToUntagLogGroupRequest() {
        final List<String> tagKeys = SET_TAGS.stream().map(Tag::getKey).collect(Collectors.toList());
        final UntagLogGroupRequest request = UntagLogGroupRequest.builder()
                .logGroupName(RESOURCE_MODEL.getLogGroupName())
                .tags(tagKeys)
                .build();

        assertThat(Translator.translateToUntagLogGroupRequest(RESOURCE_MODEL.getLogGroupName(), tagKeys))
                .isEqualToComparingFieldByField(request);
    }

    @Test
    public void testTranslateForReadResponse() {
        final LogGroup logGroup = LogGroup.builder()
            .logGroupName("LogGroup")
            .retentionInDays(1)
            .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .build();

        final DescribeLogGroupsResponse response = DescribeLogGroupsResponse.builder()
                .logGroups(Collections.singletonList(logGroup))
                .build();
        final ListTagsLogGroupResponse tagsResponse = ListTagsLogGroupResponse.builder()
                .tags(MAP_TAGS)
                .build();
        assertThat(Translator.translateForReadResponse(response, tagsResponse, "LogGroup")).isEqualToComparingFieldByField(RESOURCE_MODEL);
    }

    @Test
    public void testTranslateForReadResponse_ExactLogGroupName() {
        final LogGroup logGroup = LogGroup.builder()
                .logGroupName("LogGroup")
                .retentionInDays(1)
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
                .build();
        final LogGroup logGroup2 = LogGroup.builder()
                .logGroupName("LogGroup2")
                .retentionInDays(2)
                .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")
                .build();
        final DescribeLogGroupsResponse response = DescribeLogGroupsResponse.builder()
                .logGroups(Arrays.asList(logGroup2, logGroup))
                .build();
        final ListTagsLogGroupResponse tagsResponse = ListTagsLogGroupResponse.builder()
                .tags(MAP_TAGS)
                .build();
        assertThat(Translator.translateForReadResponse(response, tagsResponse, "LogGroup")).isEqualToComparingFieldByField(RESOURCE_MODEL);
    }

    @Test
    public void testTranslateForReadResponse_logGroupEmpty() {
        final DescribeLogGroupsResponse response = DescribeLogGroupsResponse.builder()
            .logGroups(Collections.emptyList())
            .build();
        final ListTagsLogGroupResponse tagsResponse = ListTagsLogGroupResponse.builder()
                .tags(Collections.emptyMap())
                .build();
        final ResourceModel emptyModel = ResourceModel.builder()
                .retentionInDays(null)
                .logGroupName(null)
                .tags(null)
                .build();
        assertThat(Translator.translateForReadResponse(response, tagsResponse, "LogGroup")).isEqualToComparingFieldByField(emptyModel);
    }

    @Test
    public void testTranslateForReadResponse_LogGroupHasNullMembers() {
        final DescribeLogGroupsResponse response = DescribeLogGroupsResponse.builder()
                .logGroups(Collections.singletonList(LogGroup.builder().build()))
                .build();
        final ListTagsLogGroupResponse tagsResponse = ListTagsLogGroupResponse.builder()
                .tags(Collections.emptyMap())
                .build();
        final ResourceModel emptyModel = ResourceModel.builder()
                .retentionInDays(null)
                .logGroupName(null)
                .tags(null)
                .build();
        assertThat(Translator.translateForReadResponse(response, tagsResponse, "LogGroup")).isEqualToComparingFieldByField(emptyModel);
    }

    @Test
    public void buildResourceAlreadyExistsErrorMessage() {
        final String expected = "Resource of type 'AWS::Logs::LogGroup' with identifier 'ID' already exists.";
        assertThat(Translator.buildResourceAlreadyExistsErrorMessage("ID")).isEqualTo(expected);
    }

    @Test
    public void buildResourceDoesNotExistErrorMessage() {
        final String expected = "Resource of type 'AWS::Logs::LogGroup' with identifier 'ID' was not found.";
        assertThat(Translator.buildResourceDoesNotExistErrorMessage("ID")).isEqualTo(expected);
    }

    @Test
    public void translateTagsToSdk() {
        assertThat(Translator.translateTagsToSdk(SET_TAGS)).isEqualTo(MAP_TAGS);
    }

    @Test
    public void translateTagsToSdk_TagsEmpty() {
        assertThat(Translator.translateTagsToSdk(null)).isNull();
        assertThat(Translator.translateTagsToSdk(Collections.emptySet())).isNull();
    }

    @Test
    public void translateSdkToTags() {
        assertThat(Translator.translateSdkToTags(MAP_TAGS)).isEqualTo(SET_TAGS);
    }

    @Test
    public void translateSdkToTags_TagsEmpty() {
        assertThat(Translator.translateSdkToTags(null)).isNull();
        assertThat(Translator.translateSdkToTags(Collections.emptyMap())).isNull();
    }
}
