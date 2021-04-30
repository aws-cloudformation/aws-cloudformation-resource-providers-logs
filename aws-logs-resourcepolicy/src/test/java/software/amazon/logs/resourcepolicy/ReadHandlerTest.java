package software.amazon.logs.resourcepolicy;

import com.google.common.collect.ImmutableList;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeResourcePoliciesResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourcePolicy;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {
    ReadHandler handler;
    private static final String MOCK_RESOURCEPOLICY_NAME = "someName";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        handler = new ReadHandler();
    }

    @Test
    public void handleRequest_SimpleSuccess() {

        final DescribeResourcePoliciesResponse describeResourcePoliciesResponse = DescribeResourcePoliciesResponse.builder()
                .resourcePolicies(ImmutableList.of(ResourcePolicy.builder().policyName(MOCK_RESOURCEPOLICY_NAME).build()))
                .build();
        final ResourceModel model = ResourceModel.builder().policyName(MOCK_RESOURCEPOLICY_NAME).build();
        doReturn(describeResourcePoliciesResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());
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
    /* Case to queries returned*/
    public void handleRequest_SimpleFailure() {

        final DescribeResourcePoliciesResponse describeResourcePoliciesResponse = DescribeResourcePoliciesResponse.builder()
                .resourcePolicies(ImmutableList.of(ResourcePolicy.builder().policyName("WrongQueryID").build()))
                .build();
        final ResourceModel model = ResourceModel.builder().policyName(MOCK_RESOURCEPOLICY_NAME).build();
        doReturn(describeResourcePoliciesResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }


    @Test
    public void handleRequest_ServiceUnavailable() {
        BaseTests.handleRequest_ServiceUnavailable(proxy, handler, logger, MOCK_RESOURCEPOLICY_NAME);
    }

    @Test
    public void handleRequest_InvalidParameter() {
        BaseTests.handleRequest_InvalidParameter(proxy, handler, logger, MOCK_RESOURCEPOLICY_NAME);
    }
}
