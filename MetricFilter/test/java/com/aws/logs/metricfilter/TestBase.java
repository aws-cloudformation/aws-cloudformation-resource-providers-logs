package com.aws.logs.metricfilter;

import com.aws.cfn.proxy.AmazonWebServicesClientProxy;
import com.aws.cfn.proxy.Logger;
import com.aws.cfn.proxy.ResourceHandlerRequest;
import org.mockito.Mock;

import java.util.Arrays;

public class TestBase {

    public static final String CLIENT_REQUEST_TOKEN = "ClientRequestToken";
    public static final String RESOURCE_TYPE = "AWS::Logs::MetricFilter";
    public static final String FILTER_PATTERN = "FilterPattern";
    public static final String LOG_GROUP_NAME = "LogGroupName";
    public static final String FILTER_NAME = "FilterName";
    public static final String METRIC_NAME = "MetricName";
    public static final String METRIC_NAMESPACE = "MetricNamespace";
    public static final String METRIC_VALUE = "MetricValue";
    public static final String REQUEST_ID = "RequestID";
    public static final Float DEFAULT_VALUE = 0.1f;

    public static final MetricTransformationProperty METRIC_TRANSFORMATION_PROPERTY = new MetricTransformationProperty(DEFAULT_VALUE, METRIC_NAME, METRIC_NAMESPACE, METRIC_VALUE);
    public static final ResourceModel RESOURCE_MODEL = new ResourceModel(FILTER_PATTERN, LOG_GROUP_NAME, Arrays.asList(METRIC_TRANSFORMATION_PROPERTY), FILTER_NAME);
    public static final ResourceModel RESOURCE_MODEL_UPDATE = new ResourceModel(null, LOG_GROUP_NAME, Arrays.asList(METRIC_TRANSFORMATION_PROPERTY), FILTER_NAME);

    @Mock
    protected AmazonWebServicesClientProxy proxy;

    @Mock
    protected CallbackContext callbackContext;

    @Mock
    protected Logger logger;

    @Mock
    protected ResourceHandlerRequest<ResourceModel> resourceHandlerRequest;
}
