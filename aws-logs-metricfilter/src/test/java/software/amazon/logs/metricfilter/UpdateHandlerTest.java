package software.amazon.logs.metricfilter;

import com.amazonaws.AmazonServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeMetricFiltersResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.MetricFilter;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceNotFoundException;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    private final String FILTER_NAME = "test-filter";
    private final String LOG_GROUP_NAME = "test-log-group";

    private final UpdateHandler handler = new UpdateHandler();

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final MetricFilter filter = MetricFilter.builder()
            .filterName(FILTER_NAME)
            .logGroupName(LOG_GROUP_NAME)
            .filterPattern("[test]")
            .metricTransformations(software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation.builder()
                    .metricName("metric")
                    .metricValue("value")
                    .build())
            .build();
        final DescribeMetricFiltersResponse describeResponse = DescribeMetricFiltersResponse.builder()
            .metricFilters(filter)
            .build();

        final PutMetricDataResponse createResponse = PutMetricDataResponse.builder().build();

        // return existing metrics for pre-update check and then success response
        doReturn(describeResponse)
            .doReturn(createResponse)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel desired = ResourceModel.builder()
            .filterName(FILTER_NAME)
            .logGroupName(LOG_GROUP_NAME)
            .filterPattern("some new pattern")
            .metricTransformations(Arrays.asList(
                software.amazon.logs.metricfilter.MetricTransformation.builder()
                    .metricName("metric").metricValue("value").build(),
                software.amazon.logs.metricfilter.MetricTransformation.builder()
                    .metricName("metric2").metricValue("value2").build()
            ))
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desired)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler
                .handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel()).isEqualTo(desired);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_FailedCreate_UnknownError() {
        // throw arbitrary error which should propagate to be handled by wrapper
        doThrow(SdkException.builder().message("test error").build())
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel desired = ResourceModel.builder()
            .filterName(FILTER_NAME)
            .logGroupName(LOG_GROUP_NAME)
            .filterPattern("some new pattern")
            .metricTransformations(Arrays.asList(
                software.amazon.logs.metricfilter.MetricTransformation.builder()
                    .metricName("metric").metricValue("value").build(),
                software.amazon.logs.metricfilter.MetricTransformation.builder()
                    .metricName("metric2").metricValue("value2").build()
            ))
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desired)
            .build();

        assertThrows(SdkException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    @Test
    public void handleRequest_FailedCreate_AmazonServiceException() {
        // AmazonServiceExceptions should be thrown so they can be handled by wrapper
        doThrow(new AmazonServiceException("test error"))
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel desired = ResourceModel.builder()
            .filterName(FILTER_NAME)
            .logGroupName(LOG_GROUP_NAME)
            .filterPattern("some new pattern")
            .metricTransformations(Arrays.asList(
                software.amazon.logs.metricfilter.MetricTransformation.builder()
                    .metricName("metric").metricValue("value").build(),
                software.amazon.logs.metricfilter.MetricTransformation.builder()
                    .metricName("metric2").metricValue("value2").build()
            ))
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desired)
            .build();

        assertThrows(AmazonServiceException.class, () -> {
            handler.handleRequest(proxy, request, null, logger);
        });
    }

    @Test
    public void handleRequest_NonExistingLogGroup_Failed() {
        // no matching log group throws exception
        doThrow(ResourceNotFoundException.class)
            .when(proxy)
            .injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            );

        final ResourceModel desired = ResourceModel.builder()
            .filterName(FILTER_NAME)
            .logGroupName(LOG_GROUP_NAME)
            .filterPattern("some new pattern")
            .metricTransformations(Arrays.asList(
                software.amazon.logs.metricfilter.MetricTransformation.builder()
                    .metricName("metric").metricValue("value").build(),
                software.amazon.logs.metricfilter.MetricTransformation.builder()
                    .metricName("metric2").metricValue("value2").build()
            ))
            .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desired)
            .build();

        assertThrows(software.amazon.cloudformation.exceptions.ResourceNotFoundException.class,
            () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_NotUpdatable_FilterName() {
        final ResourceModel previous = ResourceModel.builder()
                .filterName(FILTER_NAME + "a") // invalid update
                .logGroupName(LOG_GROUP_NAME)
                .metricTransformations(Arrays.asList(
                        software.amazon.logs.metricfilter.MetricTransformation.builder()
                                .metricName("metric").metricValue("value").build(),
                        software.amazon.logs.metricfilter.MetricTransformation.builder()
                                .metricName("metric2").metricValue("value2").build()
                ))
                .build();
        final ResourceModel desired = ResourceModel.builder()
                .filterName(FILTER_NAME)
                .logGroupName(LOG_GROUP_NAME)
                .metricTransformations(Arrays.asList(
                        software.amazon.logs.metricfilter.MetricTransformation.builder()
                                .metricName("metric").metricValue("value").build(),
                        software.amazon.logs.metricfilter.MetricTransformation.builder()
                                .metricName("metric2").metricValue("value2").build()
                ))
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(previous)
                .desiredResourceState(desired)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> event = handler.handleRequest(proxy, request, null, logger);
        assertThat(event.isFailed()).isTrue();
        assertThat(event.getErrorCode()).isEqualTo(HandlerErrorCode.NotUpdatable);
    }

    @Test
    public void handleRequest_NotUpdatable_LogGroupName() {
        final ResourceModel previous = ResourceModel.builder()
                .filterName(FILTER_NAME)
                .logGroupName(LOG_GROUP_NAME + "a") // invalid update
                .metricTransformations(Arrays.asList(
                        software.amazon.logs.metricfilter.MetricTransformation.builder()
                                .metricName("metric").metricValue("value").build(),
                        software.amazon.logs.metricfilter.MetricTransformation.builder()
                                .metricName("metric2").metricValue("value2").build()
                ))
                .build();
        final ResourceModel desired = ResourceModel.builder()
                .filterName(FILTER_NAME)
                .logGroupName(LOG_GROUP_NAME)
                .metricTransformations(Arrays.asList(
                        software.amazon.logs.metricfilter.MetricTransformation.builder()
                                .metricName("metric").metricValue("value").build(),
                        software.amazon.logs.metricfilter.MetricTransformation.builder()
                                .metricName("metric2").metricValue("value2").build()
                ))
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(previous)
                .desiredResourceState(desired)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> event = handler.handleRequest(proxy, request, null, logger);
        assertThat(event.isFailed()).isTrue();
        assertThat(event.getErrorCode()).isEqualTo(HandlerErrorCode.NotUpdatable);
    }

}
