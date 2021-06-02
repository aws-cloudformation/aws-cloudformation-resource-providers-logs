package software.amazon.logs.resourcepolicy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeResourcePoliciesResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutResourcePolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutResourcePolicyResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourcePolicy;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {
    private static final String MOCK_RESOURCEPOLICY_NAME = "someName";
    private static final String MOCK_RESOURCEPOLICY_POLICY = "{}";
    private UpdateHandler handler;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    DescribeResourcePoliciesResponse describeResponse;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);

        logger = mock(Logger.class);
        handler = new UpdateHandler();

        describeResponse = DescribeResourcePoliciesResponse.builder()
                .resourcePolicies(ResourcePolicy.builder().policyName(MOCK_RESOURCEPOLICY_NAME).build())
                .build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        BaseTests.stubDescribeResponse(describeResponse, proxy);

        final ResourceModel model = ResourceModel.builder().
                policyName(MOCK_RESOURCEPOLICY_NAME).
                policyDocument(MOCK_RESOURCEPOLICY_POLICY).
                build();
        PutResourcePolicyResponse putResourcePolicyResponse = PutResourcePolicyResponse.builder()
                .resourcePolicy(ResourcePolicy.builder().policyName(MOCK_RESOURCEPOLICY_NAME).build())
                .build();

        doReturn(putResourcePolicyResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(
                        ArgumentMatchers.isA(PutResourcePolicyRequest.class),
                        ArgumentMatchers.any()
                );

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

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

        BaseTests.stubDescribeResponse(noPoliciesResponse, proxy);

        ResourceModel model = ResourceModel.builder().policyName(MOCK_RESOURCEPOLICY_NAME).policyDocument("{}").build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        assertThrows(CfnNotFoundException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_InvalidParameter() {
        BaseTests.stubDescribeResponse(describeResponse, proxy);

        BaseTests.handleRequest_InvalidParameter(proxy, handler, logger, MOCK_RESOURCEPOLICY_NAME, PutResourcePolicyRequest.class);
    }

    @Test
    public void handleRequest_ServiceUnavailable() {
        BaseTests.stubDescribeResponse(describeResponse, proxy);

        BaseTests.handleRequest_ServiceUnavailable(proxy, handler, logger, MOCK_RESOURCEPOLICY_NAME, PutResourcePolicyRequest.class);
    }
}
