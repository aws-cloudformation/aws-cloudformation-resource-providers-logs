package software.amazon.logs.querydefinitions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import javax.annotation.Nullable;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class BaseTests {
    private static final String MOCK_ERROR = "someError";


    @Test
    public static void handleRequest_ResourceNotFound(AmazonWebServicesClientProxy proxy, BaseHandler<?> handler, Logger logger, @Nullable String queryDefinitionId) {

        final ResourceModel model = ResourceModel.builder().queryDefinitionId(queryDefinitionId).build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        doThrow(ResourceNotFoundException.builder().message(MOCK_ERROR).build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        assertThrows(CfnNotFoundException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public static void handleRequest_ServiceUnavailable(AmazonWebServicesClientProxy proxy, BaseHandler<?> handler, Logger logger, @Nullable String queryDefinitionId) {
        final ResourceModel model = ResourceModel.builder().queryDefinitionId(queryDefinitionId).build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        doThrow(ServiceUnavailableException.builder().message(MOCK_ERROR).build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        assertThrows(CfnServiceInternalErrorException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public static void handleRequest_InvalidParameter(AmazonWebServicesClientProxy proxy, BaseHandler<?> handler, Logger logger, @Nullable String queryDefinitionId) {

        final ResourceModel model = ResourceModel.builder().queryDefinitionId(queryDefinitionId).build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        doThrow(InvalidParameterException.builder().message(MOCK_ERROR).build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        assertThrows(CfnInvalidRequestException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }
}
