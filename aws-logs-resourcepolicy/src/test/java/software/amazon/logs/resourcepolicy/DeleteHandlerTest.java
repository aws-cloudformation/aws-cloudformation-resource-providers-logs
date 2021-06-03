package software.amazon.logs.resourcepolicy;

import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteResourcePolicyRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteResourcePolicyResponse;
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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest {
    private static final String MOCK_RESOURCEPOLICY_NAME = "someName";
    private static final String MOCK_ERROR = "someError";
    private DeleteHandler handler;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        handler = new DeleteHandler();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceModel model = ResourceModel.builder().policyName(MOCK_RESOURCEPOLICY_NAME).build();

        final DeleteResourcePolicyResponse deleteResponse = DeleteResourcePolicyResponse.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        doReturn(deleteResponse)
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }


    @Test
    public void handleRequest_InvalidParameter() {
        BaseTests.handleRequest_InvalidParameter(proxy, handler, logger, MOCK_RESOURCEPOLICY_NAME, DeleteResourcePolicyRequest.class);
    }

    @Test
    public void handleRequest_ResourceNotFound() {
        BaseTests.handleRequest_ResourceNotFound(proxy, handler, logger, MOCK_RESOURCEPOLICY_NAME, DeleteResourcePolicyRequest.class);
    }

    @Test
    public void handleRequest_ServiceUnavailable() {
        BaseTests.handleRequest_ServiceUnavailable(proxy, handler, logger, MOCK_RESOURCEPOLICY_NAME, DeleteResourcePolicyRequest.class);
    }
}
