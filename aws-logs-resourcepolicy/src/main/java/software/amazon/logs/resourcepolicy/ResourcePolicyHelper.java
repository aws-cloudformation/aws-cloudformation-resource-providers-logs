package software.amazon.logs.resourcepolicy;

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeResourcePoliciesResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;

public class ResourcePolicyHelper {
    public static boolean exists(AmazonWebServicesClientProxy proxy, ResourceModel model) {
        String nextToken = null;
        do {
            DescribeResourcePoliciesResponse response = proxy.injectCredentialsAndInvokeV2(
                    Translator.translateToListRequest(nextToken),
                    ClientBuilder.getLogsClient()::describeResourcePolicies);


            boolean found = response.resourcePolicies().stream().anyMatch(
                    policy -> (policy.policyName().equals(model.getPolicyName())));

            if (found) {
                return true;
            }
            nextToken = response.nextToken();

        } while (nextToken != null);

        return false;
    }
}
