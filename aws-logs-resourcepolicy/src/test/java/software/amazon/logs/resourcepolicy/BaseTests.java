package software.amazon.logs.resourcepolicy;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidParameterException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchlogs.model.ServiceUnavailableException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import javax.annotation.Nullable;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class BaseTests {
    private static final String MOCK_ERROR = "someError";

    public static void handleRequest_ResourceNotFound(AmazonWebServicesClientProxy proxy, BaseHandler<?> handler, Logger logger, @Nullable String name) {

        final ResourceModel model = dummyModel(name);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        doThrow(ResourceNotFoundException.builder().message(MOCK_ERROR).build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        assertThrows(CfnNotFoundException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    public static void handleRequest_ResourceNotFound(AmazonWebServicesClientProxy proxy, BaseHandlerStd handler, Logger logger, @Nullable String name, ProxyClient<CloudWatchLogsClient> proxyClient) {
        final ResourceModel model = dummyModel(name);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () -> handler.handleRequest(proxy, request, null, proxyClient, logger));
    }

    public static void handleRequest_ServiceUnavailable(AmazonWebServicesClientProxy proxy, BaseHandler<?> handler, Logger logger, @Nullable String name) {
        final ResourceModel model = dummyModel(name);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        doThrow(ServiceUnavailableException.builder().message(MOCK_ERROR).build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        assertThrows(CfnServiceInternalErrorException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    public static void handleRequest_ServiceUnavailable(AmazonWebServicesClientProxy proxy, BaseHandlerStd handler, Logger logger, @Nullable String name, ProxyClient<CloudWatchLogsClient> proxyClient) {
        final ResourceModel model = dummyModel(name);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        doThrow(ServiceUnavailableException.builder().message(MOCK_ERROR).build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        assertThrows(CfnServiceInternalErrorException.class, () -> handler.handleRequest(proxy, request, null, proxyClient, logger));
    }

    public static void handleRequest_InvalidParameter(AmazonWebServicesClientProxy proxy, BaseHandler<?> handler, Logger logger, @Nullable String name) {

        final ResourceModel model = dummyModel(name);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        doThrow(InvalidParameterException.builder().message(MOCK_ERROR).build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        assertThrows(CfnInvalidRequestException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    public static void handleRequest_InvalidParameter(AmazonWebServicesClientProxy proxy, BaseHandlerStd handler, Logger logger, @Nullable String name, ProxyClient<CloudWatchLogsClient> proxyClient) {

        final ResourceModel model = dummyModel(name);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        doThrow(InvalidParameterException.builder().message(MOCK_ERROR).build())
                .when(proxy)
                .injectCredentialsAndInvokeV2(ArgumentMatchers.any(), ArgumentMatchers.any());

        assertThrows(CfnInvalidRequestException.class, () -> handler.handleRequest(proxy, request, null, proxyClient, logger));
    }

    private static ResourceModel dummyModel(String name) {
        return ResourceModel.builder().policyName(name).policyDocument("{}").build();
    }
}
