package software.amazon.logs.loggroup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogGroupsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogGroup;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DisassociateKmsKeyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.AssociateKmsKeyRequest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class TranslatorTest {
    private static final ResourceModel RESOURCE_MODEL = ResourceModel.builder()
        .retentionInDays(1)
        .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        .logGroupName("LogGroup")
        .build();

    @Test
    public void testTranslateToRead() {
        final DescribeLogGroupsRequest request = DescribeLogGroupsRequest.builder()
            .logGroupNamePrefix(RESOURCE_MODEL.getLogGroupName())
            .build();
        assertThat(Translator.translateToReadRequest(RESOURCE_MODEL)).isEqualToComparingFieldByField(request);
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
            .build();
        assertThat(Translator.translateToCreateRequest(RESOURCE_MODEL)).isEqualToComparingFieldByField(request);
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
    public void testTranslateForRead() {
        final LogGroup logGroup = LogGroup.builder()
            .logGroupName("LogGroup")
            .retentionInDays(1)
            .kmsKeyId("arn:aws:kms:us-east-1:$123456789012:key/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
            .build();

        final DescribeLogGroupsResponse response = DescribeLogGroupsResponse.builder()
                .logGroups(Collections.singletonList(logGroup))
                .build();
        assertThat(Translator.translateForRead(response)).isEqualToComparingFieldByField(RESOURCE_MODEL);
    }

    @Test
    public void testTranslateForRead_logGroupEmpty() {
        final DescribeLogGroupsResponse response = DescribeLogGroupsResponse.builder()
            .logGroups(Collections.emptyList())
            .build();
        final ResourceModel emptyModel = ResourceModel.builder()
                .retentionInDays(null)
                .logGroupName(null)
                .build();
        assertThat(Translator.translateForRead(response)).isEqualToComparingFieldByField(emptyModel);
    }

    @Test
    public void testTranslateForRead_LogGroupHasNullMembers() {
        final DescribeLogGroupsResponse response = DescribeLogGroupsResponse.builder()
                .logGroups(Collections.singletonList(LogGroup.builder().build()))
                .build();
        final ResourceModel emptyModel = ResourceModel.builder()
            .retentionInDays(null)
            .logGroupName(null)
            .build();
        assertThat(Translator.translateForRead(response)).isEqualToComparingFieldByField(emptyModel);
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
}
