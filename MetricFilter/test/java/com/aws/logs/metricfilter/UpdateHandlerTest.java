package com.aws.logs.metricfilter;

import com.aws.cfn.proxy.AmazonWebServicesClientProxy;
import com.aws.cfn.proxy.Logger;
import com.aws.cfn.proxy.ProgressEvent;
import com.aws.cfn.proxy.ResourceHandlerRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsResponseMetadata;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterResponse;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateHandlerTest {

    @Mock
    protected Logger logger;

    @Mock
    private PutMetricFilterResponse response;

    @Mock
    protected CallbackContext callbackContext;

    @Mock
    private CloudWatchLogsResponseMetadata metadata;

    @Mock
    protected AmazonWebServicesClientProxy proxy;

    @Mock
    protected ResourceHandlerRequest<ResourceModel> resourceHandlerRequest;


    private UpdateHandler updateHandler;
    private ResourceModel resourceModel;
    private ResourceModel resourceModelUpdate;
    @Before
    public void setup() {
        updateHandler = new UpdateHandler();
        resourceModel = new ResourceModel("FilterPattern", "LogGroupName", Arrays.asList(new MetricTransformationProperty(0.1f, "MetricName", "MetricNamespace", "MetricValue")), "FilterName");
        resourceModelUpdate = new ResourceModel("FilterPatternUpdate", "LogGroupName", Arrays.asList(new MetricTransformationProperty(0.1f, "MetricName", "MetricNamespace", "MetricValue")), "FilterName");
        when(resourceHandlerRequest.getDesiredResourceState()).thenReturn(resourceModelUpdate);
        when(resourceHandlerRequest.getPreviousResourceState()).thenReturn(resourceModel);
    }

    @Test
    public void testSuccessfulUpdate() {
        final PutMetricFilterRequest putMetricFilterRequest = PutMetricFilterRequest
                .builder()
                .logGroupName(resourceModelUpdate.getLogGroupName())
                .filterName(resourceModelUpdate.getFilterName())
                .filterPattern(resourceModelUpdate.getFilterPattern())
                .metricTransformations(Utils.translateMetricTransformations(resourceModelUpdate))
                .build();
        when(proxy.injectCredentialsAndInvokeV2(eq(putMetricFilterRequest), any())).thenReturn(response);
        when(response.responseMetadata()).thenReturn(metadata);
        when(metadata.requestId()).thenReturn("RequestID");
        final ProgressEvent pe = updateHandler.handleRequest(proxy, resourceHandlerRequest, callbackContext, logger);

        verify(proxy).injectCredentialsAndInvokeV2(eq(putMetricFilterRequest), any());
        assertThat(pe, is(equalTo(Utils.defaultSuccessHandler(resourceModelUpdate))));
    }
}
