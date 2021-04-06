package software.amazon.logs.resourcepolicy;

import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeResourcePoliciesResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutResourcePolicyResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourcePolicy;
import software.amazon.cloudformation.proxy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {
    private static final String MOCK_RESOURCEPOLICY_NAME = "someName";
    private static final String MOCK_RESOURCEPOLICY_POLICY = "{}";
    private UpdateHandler handler;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<CloudWatchLogsClient> proxyClient;

    @Mock
    CloudWatchLogsClient sdkClient;

    @Mock
    private Logger logger;

    DescribeResourcePoliciesResponse describeResponse;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        sdkClient = mock(CloudWatchLogsClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);

        logger = mock(Logger.class);
        handler = new UpdateHandler();

        describeResponse = DescribeResourcePoliciesResponse.builder()
                .resourcePolicies(ResourcePolicy.builder().policyName(MOCK_RESOURCEPOLICY_NAME).build())
                .build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        when(proxyClient.client().describeResourcePolicies())
                .thenReturn(describeResponse);

        final ResourceModel model = ResourceModel.builder().
                policyName(MOCK_RESOURCEPOLICY_NAME).
                policyDocument(MOCK_RESOURCEPOLICY_POLICY).
                build();
        PutResourcePolicyResponse putResourcePolicyResponse = PutResourcePolicyResponse.builder()
                .resourcePolicy(ResourcePolicy.builder().policyName(MOCK_RESOURCEPOLICY_NAME).build())
                .build();

        doReturn(putResourcePolicyResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_NameNull_Failure() {
        software.amazon.logs.resourcepolicy.ResourceModel expectedModel = software.amazon.logs.resourcepolicy.ResourceModel.builder()
                .policyName(null)
                .policyDocument(MOCK_RESOURCEPOLICY_POLICY)
                .build();
        final ResourceHandlerRequest<software.amazon.logs.resourcepolicy.ResourceModel> request = ResourceHandlerRequest.<software.amazon.logs.resourcepolicy.ResourceModel>builder()
                .desiredResourceState(expectedModel)
                .build();
        final ProgressEvent<software.amazon.logs.resourcepolicy.ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }

    @Test
    public void handleRequest_PolicyDocumentNull_Failure() {
        software.amazon.logs.resourcepolicy.ResourceModel expectedModel = software.amazon.logs.resourcepolicy.ResourceModel.builder()
                .policyName("someName")
                .policyDocument(null)
                .build();
        final ResourceHandlerRequest<software.amazon.logs.resourcepolicy.ResourceModel> request = ResourceHandlerRequest.<software.amazon.logs.resourcepolicy.ResourceModel>builder()
                .desiredResourceState(expectedModel)
                .build();
        final ProgressEvent<software.amazon.logs.resourcepolicy.ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }

    @Test
    public void handleRequest_ResourceNotFound() {
        final DescribeResourcePoliciesResponse noPoliciesResponse = DescribeResourcePoliciesResponse.builder().build();

        when(proxyClient.client().describeResourcePolicies())
                .thenReturn(noPoliciesResponse);

        BaseTests.handleRequest_ResourceNotFound(proxy, handler, logger, MOCK_RESOURCEPOLICY_NAME, proxyClient);
    }

    @Test
    public void handleRequest_InvalidParameter() {
        when(proxyClient.client().describeResourcePolicies())
                .thenReturn(describeResponse);

        BaseTests.handleRequest_InvalidParameter(proxy, handler, logger, MOCK_RESOURCEPOLICY_NAME, proxyClient);
    }

    @Test
    public void handleRequest_ServiceUnavailable() {
        when(proxyClient.client().describeResourcePolicies())
                .thenReturn(describeResponse);

        BaseTests.handleRequest_ServiceUnavailable(proxy, handler, logger, MOCK_RESOURCEPOLICY_NAME, proxyClient);
    }
}
