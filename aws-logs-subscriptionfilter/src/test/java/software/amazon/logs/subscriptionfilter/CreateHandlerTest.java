package software.amazon.logs.subscriptionfilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsException;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeSubscriptionFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.OperationAbortedException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutSubscriptionFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutSubscriptionFilterResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudwatchlogs.emf.logger.MetricsLogger;

@ExtendWith(MockitoExtension.class)
class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<CloudWatchLogsClient> proxyClient;

    @Mock
    CloudWatchLogsClient sdkClient;

    @Mock
    private MetricsLogger metrics;

    final CreateHandler handler = new CreateHandler();

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(CloudWatchLogsClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        metrics = mock(MetricsLogger.class);
    }

    @AfterEach
    public void tear_down() {
        verify(sdkClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(sdkClient);
    }

    @Test
    void handleRequest_Success() {
        final ResourceModel model = buildDefaultModel();
        final DescribeSubscriptionFiltersResponse describeResponse = DescribeSubscriptionFiltersResponse
            .builder()
            .subscriptionFilters(Translator.translateToSDK(model))
            .build();

        final PutSubscriptionFilterResponse createResponse = PutSubscriptionFilterResponse.builder().build();

        when(proxyClient.client().describeSubscriptionFilters(any(DescribeSubscriptionFiltersRequest.class)))
            .thenThrow(ResourceNotFoundException.class)
            .thenReturn(describeResponse);

        when(proxyClient.client().putSubscriptionFilter(any(PutSubscriptionFilterRequest.class))).thenReturn(createResponse);

        final ResourceHandlerRequest<ResourceModel> request = buildResourceHandlerRequest(model);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
            proxy,
            request,
            new CallbackContext(),
            proxyClient,
            logger,
            metrics
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        verify(proxyClient.client(), times(1)).describeSubscriptionFilters(any(DescribeSubscriptionFiltersRequest.class));
        verify(proxyClient.client()).putSubscriptionFilter(any(PutSubscriptionFilterRequest.class));
    }

    @Test
    void handleRequest_Success2() {
        final ResourceModel model = buildDefaultModel();
        final PutSubscriptionFilterResponse createResponse = PutSubscriptionFilterResponse.builder().build();

        // return no existing Subscriptions for pre-create and then success response for create
        when(proxyClient.client().describeSubscriptionFilters(any(DescribeSubscriptionFiltersRequest.class)))
            .thenThrow(ResourceNotFoundException.class)
            .thenReturn(DescribeSubscriptionFiltersResponse.builder().subscriptionFilters(Translator.translateToSDK(model)).build());

        when(proxyClient.client().putSubscriptionFilter(any(PutSubscriptionFilterRequest.class))).thenReturn(createResponse);

        final ResourceHandlerRequest<ResourceModel> request = buildResourceHandlerRequest(model);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
            proxy,
            request,
            new CallbackContext(),
            proxyClient,
            logger,
            metrics
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        verify(proxyClient.client(), times(1)).describeSubscriptionFilters(any(DescribeSubscriptionFiltersRequest.class));
        verify(proxyClient.client()).putSubscriptionFilter(any(PutSubscriptionFilterRequest.class));
    }

    @Test
    void handleRequest_Success_WithGeneratedName() {
        // no filter name supplied; should be generated
        final ResourceModel model = ResourceModel.builder().logGroupName("test-log-group").filterPattern("some pattern").build();

        when(proxyClient.client().describeSubscriptionFilters(any(DescribeSubscriptionFiltersRequest.class)))
            .thenThrow(ResourceNotFoundException.class)
            .thenReturn(DescribeSubscriptionFiltersResponse.builder().subscriptionFilters(Translator.translateToSDK(model)).build());

        when(proxyClient.client().putSubscriptionFilter(any(PutSubscriptionFilterRequest.class)))
            .thenReturn(PutSubscriptionFilterResponse.builder().build());

        final ResourceHandlerRequest<ResourceModel> request = buildResourceHandlerRequest(model);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
            proxy,
            request,
            new CallbackContext(),
            proxyClient,
            logger,
            metrics
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    void handleRequest_Should_ThrowException_When_PutOperationIsAborted() {
        final ResourceModel model = buildDefaultModel();

        when(proxyClient.client().describeSubscriptionFilters(any(DescribeSubscriptionFiltersRequest.class)))
            .thenThrow(ResourceNotFoundException.class)
            .thenReturn(DescribeSubscriptionFiltersResponse.builder().subscriptionFilters(Translator.translateToSDK(model)).build());

        when(proxyClient.client().putSubscriptionFilter(any(PutSubscriptionFilterRequest.class)))
            .thenThrow(OperationAbortedException.class);

        final ResourceHandlerRequest<ResourceModel> request = buildResourceHandlerRequest(model);

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics))
            .isInstanceOf(CfnResourceConflictException.class);
    }

    @ParameterizedTest
    @MethodSource("alreadyExists")
    void handleRequest_Should_ReturnFailureProgressEvent_When_SubscriptionFilterExists(
        final String filterName1,
        final String filterName2,
        final boolean equal
    ) {
        final ResourceModel model1 = buildDefaultModel();
        final ResourceModel model2 = buildDefaultModel();
        model1.setFilterName(filterName1);
        model2.setFilterName(filterName2);

        when(proxyClient.client().describeSubscriptionFilters(any(DescribeSubscriptionFiltersRequest.class)))
            .thenReturn(DescribeSubscriptionFiltersResponse.builder().subscriptionFilters(Translator.translateToSDK(model1)).build());

        final ResourceHandlerRequest<ResourceModel> request = buildResourceHandlerRequest(model2);

        if (equal) {
            assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics))
                .isInstanceOf(CfnAlreadyExistsException.class);
        } else {
            final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                proxy,
                request,
                new CallbackContext(),
                proxyClient,
                logger,
                metrics
            );

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
            assertThat(response.getCallbackDelaySeconds()).isZero();
            assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
            assertThat(response.getResourceModels()).isNull();
            assertThat(response.getMessage()).isNull();
            assertThat(response.getErrorCode()).isNull();
            verify(proxyClient.client(), times(1)).describeSubscriptionFilters(any(DescribeSubscriptionFiltersRequest.class));
            verify(proxyClient.client()).putSubscriptionFilter(any(PutSubscriptionFilterRequest.class));
        }
    }

    private static Stream<Arguments> alreadyExists() {
        return Stream.of(
            Arguments.of("subscription-name-suffix", "subscription-name", false),
            Arguments.of("subscription-name", "subscription-name", true),
            Arguments.of("subscription-name", "Subscription-Name", false),
            Arguments.of("subscription-name", "subscription-name-suffix", false),
            Arguments.of("subscription-name", "", false)
        );
    }

    @Test
    void handleRequest_Should_ReturnFailureException_When_SubscriptionFilterReadFailsWithCloudWatchLogsException() {
        when(proxyClient.client().describeSubscriptionFilters(any(DescribeSubscriptionFiltersRequest.class)))
            .thenThrow(CloudWatchLogsException.class);

        final ResourceHandlerRequest<ResourceModel> request = buildResourceHandlerRequest(buildDefaultModel());

        assertThatThrownBy(() -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger, metrics))
            .isInstanceOf(CfnGeneralServiceException.class);
    }

    @Test
    void handleRequest_Should_ReturnSuccess_When_FilterEmpty_and_RoleEmpty_and_DistributionEmpty() {
        final ResourceModel model = buildDefaultModel();
        model.setFilterPattern(null);
        model.setRoleArn(null);
        model.setDistribution(null);

        when(proxyClient.client().describeSubscriptionFilters(any(DescribeSubscriptionFiltersRequest.class)))
            .thenThrow(ResourceNotFoundException.class)
            .thenReturn(DescribeSubscriptionFiltersResponse.builder().subscriptionFilters(Translator.translateToSDK(model)).build());

        final PutSubscriptionFilterResponse putSubscriptionFilterResponse = PutSubscriptionFilterResponse.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = buildResourceHandlerRequest(model);

        when(proxyClient.client().putSubscriptionFilter(any(PutSubscriptionFilterRequest.class))).thenReturn(putSubscriptionFilterResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
            proxy,
            request,
            new CallbackContext(),
            proxyClient,
            logger,
            metrics
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    private ResourceHandlerRequest<ResourceModel> buildResourceHandlerRequest(final ResourceModel model) {
        return ResourceHandlerRequest
            .<ResourceModel>builder()
            .desiredResourceState(model)
            .logicalResourceIdentifier("logicalResourceIdentifier")
            .clientRequestToken("requestToken")
            .build();
    }
}
