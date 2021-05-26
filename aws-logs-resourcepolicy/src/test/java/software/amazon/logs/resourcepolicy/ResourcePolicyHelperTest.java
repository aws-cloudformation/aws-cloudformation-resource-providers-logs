package software.amazon.logs.resourcepolicy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeResourcePoliciesRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeResourcePoliciesResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutRetentionPolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourcePolicy;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class ResourcePolicyHelperTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    private DescribeResourcePoliciesResponse describeResponse;
    private ResourceModel model;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        model = ResourceModel.builder().policyName("myResourcePolicy").build();
    }

    @Test
    public void exists_Success() {
        ResourcePolicy returnedPolicy = ResourcePolicy.builder().policyName(model.getPolicyName()).build();
        describeResponse = DescribeResourcePoliciesResponse.builder().resourcePolicies(returnedPolicy).build();

        doReturn(describeResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.isA(DescribeResourcePoliciesRequest.class),
                        ArgumentMatchers.any()
                );

        boolean result = ResourcePolicyHelper.exists(proxy, model);
        assertThat(result).isEqualTo(true);
    }

    @Test
    public void exists_Success_MultipleDescribeRequests() {
        ResourcePolicy differentPolicy = ResourcePolicy.builder().policyName("foo").build();
        describeResponse = DescribeResourcePoliciesResponse.builder().resourcePolicies(differentPolicy).nextToken("bar").build();

        ResourcePolicy matchingPolicy = ResourcePolicy.builder().policyName(model.getPolicyName()).build();
        DescribeResourcePoliciesResponse describeResponseFound = DescribeResourcePoliciesResponse.builder()
                .resourcePolicies(matchingPolicy).build();

        doReturn(describeResponse, describeResponseFound)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.isA(DescribeResourcePoliciesRequest.class),
                        ArgumentMatchers.any()
                );

        boolean result = ResourcePolicyHelper.exists(proxy, model);
        assertThat(result).isEqualTo(true);
    }

    @Test
    public void exists_NotFound() {
        ResourcePolicy returnedPolicy = ResourcePolicy.builder().policyName("foo").build();
        describeResponse = DescribeResourcePoliciesResponse.builder().resourcePolicies(returnedPolicy).build();

        doReturn(describeResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.isA(DescribeResourcePoliciesRequest.class),
                        ArgumentMatchers.any()
                );

        boolean result = ResourcePolicyHelper.exists(proxy, model);
        assertThat(result).isEqualTo(false);
    }
}