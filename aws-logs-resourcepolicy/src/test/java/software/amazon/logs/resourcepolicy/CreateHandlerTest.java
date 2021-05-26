package software.amazon.logs.resourcepolicy;

import com.google.common.collect.ImmutableList;
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
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {
    private static final String MOCK_RESOURCEPOLICY_NAME = "someName";
    private static final String POLICY_DOC = "{\n" +
            "  \"Version\": \"2012-10-17\",\n" +
            "  \"Statement\": [\n" +
            "    {\n" +
            "      \"Effect\": \"Allow\",\n" +
            "      \"Principal\": {\n" +
            "        \"Service\": \"es.amazonaws.com\"\n" +
            "      },\n" +
            "      \"Action\": [\n" +
            "        \"logs:PutLogEvents\",\n" +
            "        \"logs:CreateLogStream\"\n" +
            "      ],\n" +
            "      \"Resource\": \"/aws/aes/domains/*\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    private CreateHandler handler;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    PutResourcePolicyResponse putResourcePolicyResponse;
    DescribeResourcePoliciesResponse describeResponse;

    ImmutableList<ResourceModel> modelsUnderTestValid = ImmutableList.of(
            ResourceModel.builder().policyName("myResourcePolicy").policyDocument(POLICY_DOC).build()
    );

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);

        logger = mock(Logger.class);
        putResourcePolicyResponse = PutResourcePolicyResponse.builder()
                .resourcePolicy(ResourcePolicy.builder().policyName(MOCK_RESOURCEPOLICY_NAME).build())
                .build();
        describeResponse = DescribeResourcePoliciesResponse.builder().build();
        handler = new CreateHandler();
    }

    @Test
    public void handleRequest_Success_ValidCases() {
        BaseTests.stubDescribeResponse(describeResponse, proxy);

        doReturn(putResourcePolicyResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.isA(PutResourcePolicyRequest.class), ArgumentMatchers.any());

        for (ResourceModel model : modelsUnderTestValid) {
            final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                    .desiredResourceState(model)
                    .build();

            final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
            assertThat(response.getCallbackContext()).isNull();
            assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
            assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
            assertThat(response.getResourceModels()).isNull();
            assertThat(response.getMessage()).isNull();
            assertThat(response.getErrorCode()).isNull();
        }
    }

    @Test
    public void handleRequest_Failed_AlreadyExists() {
        final ResourceModel model = ResourceModel.builder().policyName("myResourcePolicy").policyDocument(POLICY_DOC).build();

        describeResponse = DescribeResourcePoliciesResponse.builder()
                .resourcePolicies(ResourcePolicy.builder().policyName(model.getPolicyName()).build())
                .build();

        BaseTests.stubDescribeResponse(describeResponse, proxy);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), logger))
                .isInstanceOf(CfnAlreadyExistsException.class);
    }

    @Test
    public void handleRequest_Failure_InvalidRequest() {
        BaseTests.stubDescribeResponse(describeResponse, proxy);

        BaseTests.handleRequest_InvalidParameter(proxy, handler, logger, null, PutResourcePolicyRequest.class);
    }

    @Test
    public void handleRequest_Failure_ServiceUnavailable() {
        BaseTests.stubDescribeResponse(describeResponse, proxy);

        BaseTests.handleRequest_ServiceUnavailable(proxy, handler, logger, null, PutResourcePolicyRequest.class);
    }

    @Test
    public void handleRequest_Failure_LimitExceeded() {
        BaseTests.stubDescribeResponse(describeResponse, proxy);

        BaseTests.handleRequest_LimitExceeded(proxy, handler, logger, null, PutResourcePolicyRequest.class);
    }
}
