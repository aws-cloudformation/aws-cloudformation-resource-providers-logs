package com.aws.logs.metricfilter;

import com.aws.cfn.proxy.ProgressEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.services.cloudwatchlogs.model.CloudWatchLogsResponseMetadata;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutMetricFilterResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreateHandlerTest extends TestBase {

    @Mock
    private PutMetricFilterResponse response;

    @Mock
    private CloudWatchLogsResponseMetadata metadata;

    private CreateHandler createHandler;

    @Before
    public void setup() {
        createHandler = new CreateHandler();
        when(resourceHandlerRequest.getDesiredResourceState()).thenReturn(RESOURCE_MODEL);
    }

    @Test
    public void testSuccessfulCreate() throws Exception {
        final PutMetricFilterRequest putMetricFilterRequest = PutMetricFilterRequest
                .builder()
                .logGroupName(LOG_GROUP_NAME)
                .filterName(FILTER_NAME)
                .filterPattern(FILTER_PATTERN)
                .metricTransformations(Utils.translateMetricTransformations(RESOURCE_MODEL))
                .build();
        when(proxy.injectCredentialsAndInvokeV2(eq(putMetricFilterRequest), any())).thenReturn(response);
        when(response.responseMetadata()).thenReturn(metadata);
        when(metadata.requestId()).thenReturn(REQUEST_ID);
        final ProgressEvent pe = createHandler.handleRequest(proxy, resourceHandlerRequest, callbackContext, logger);

        verify(proxy).injectCredentialsAndInvokeV2(eq(putMetricFilterRequest), any());
        assertThat(pe, is(equalTo(Utils.defaultSuccessHandler(RESOURCE_MODEL))));
    }
}
