package software.amazon.logs.resourcepolicy;

import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteResourcePolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeResourcePoliciesRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutResourcePolicyRequest;

final class Translator {

    public static final int RESOURCE_POLICIES_LIMIT = 10;

    static PutResourcePolicyRequest translateToPutRequest(final ResourceModel model) {
        return PutResourcePolicyRequest.builder()
                .policyName(model.getPolicyName())
                .policyDocument(model.getPolicyDocument())
                .build();
    }

    static DeleteResourcePolicyRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteResourcePolicyRequest.builder()
                .policyName(model.getPolicyName())
                .build();
    }

    static DescribeResourcePoliciesRequest translateToListRequest(final String nextToken) {
        return DescribeResourcePoliciesRequest.builder()
                .nextToken(nextToken)
                .limit(RESOURCE_POLICIES_LIMIT)
                .build();
    }
}
