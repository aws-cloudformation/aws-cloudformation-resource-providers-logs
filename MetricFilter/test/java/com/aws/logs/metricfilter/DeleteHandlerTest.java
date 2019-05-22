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
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DeleteMetricFilterResponse;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeleteHandlerTest {

    @Mock
    protected Logger logger;

    @Mock
    private DeleteMetricFilterResponse response;

    @Mock
    protected CallbackContext callbackContext;

    @Mock
    private CloudWatchLogsResponseMetadata metadata;

    @Mock
    protected AmazonWebServicesClientProxy proxy;

    @Mock
    protected ResourceHandlerRequest<ResourceModel> resourceHandlerRequest;

    private DeleteHandler deleteHandler;

    private ResourceModel resourceModel;
    @Before
    public void setup() {
        deleteHandler = new DeleteHandler();
        resourceModel = new ResourceModel("FilterPattern", "LogGroupName", Arrays.asList(new MetricTransformationProperty(0.1f, "MetricName", "MetricNamespace", "MetricValue")), "FilterName");
        when(resourceHandlerRequest.getDesiredResourceState()).thenReturn(resourceModel);
    }

    @Test
    public void testSuccessfulDelete() {
        final DeleteMetricFilterRequest deleteMetricFilterRequest = DeleteMetricFilterRequest
                .builder()
                .logGroupName(resourceModel.getLogGroupName())
                .filterName(resourceModel.getFilterName())
                .build();
        when(proxy.injectCredentialsAndInvokeV2(eq(deleteMetricFilterRequest), any())).thenReturn(response);
        when(response.responseMetadata()).thenReturn(metadata);
        when(metadata.requestId()).thenReturn("RequestID");
        final ProgressEvent pe = deleteHandler.handleRequest(proxy, resourceHandlerRequest, callbackContext, logger);

        verify(proxy).injectCredentialsAndInvokeV2(eq(deleteMetricFilterRequest), any());
        assertThat(pe, is(equalTo(Utils.defaultSuccessHandler(null))));
    }
}
