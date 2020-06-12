package software.amazon.logs.destination;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeDestinationsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    private ResourceModel testResourceModel;

    private ListHandler handler;

    @BeforeEach
    public void setup() {

        testResourceModel = getTestResourceModel();
        handler = new ListHandler();
    }

    @Test
    public void handleRequest_ShouldReturnSuccess_When_DestinationIsFound() {

        final ResourceHandlerRequest<ResourceModel> request =
                ResourceHandlerRequest.<ResourceModel>builder().desiredResourceState(testResourceModel)
                        .build();
        final DescribeDestinationsResponse describeResponse = DescribeDestinationsResponse.builder()
                .destinations(getTestDestination())
                .build();

        Mockito.when(proxy.injectCredentialsAndInvokeV2(any(DescribeDestinationsRequest.class), any()))
                .thenReturn(describeResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, null, logger);

        Assertions.assertThat(response)
                .isNotNull();
        Assertions.assertThat(response.getStatus())
                .isEqualTo(OperationStatus.SUCCESS);
        Assertions.assertThat(response.getCallbackContext())
                .isNull();
        Assertions.assertThat(response.getCallbackDelaySeconds())
                .isEqualTo(0);
        Assertions.assertThat(response.getResourceModel())
                .isNull();
        Assertions.assertThat(response.getResourceModels())
                .isNotNull();
        Assertions.assertThat(response.getMessage())
                .isNull();
        Assertions.assertThat(response.getErrorCode())
                .isNull();
    }

    @Test
    public void handleRequest_ShouldThrowInternalFailureException_When_ServiceIsUnavailable() {

        final ResourceHandlerRequest<ResourceModel> request =
                ResourceHandlerRequest.<ResourceModel>builder().desiredResourceState(testResourceModel)
                        .build();

        Mockito.when(proxy.injectCredentialsAndInvokeV2(any(DescribeDestinationsRequest.class), any()))
                .thenThrow(ServiceUnavailableException.class);

        assertThrows(CfnServiceInternalErrorException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

}
