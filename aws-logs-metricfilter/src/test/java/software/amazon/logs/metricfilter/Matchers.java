package software.amazon.logs.metricfilter;

import software.amazon.awssdk.services.cloudwatchlogs.model.MetricFilter;

import static software.amazon.logs.metricfilter.Translator.translateFromSDK;
import static org.assertj.core.api.Assertions.assertThat;

public class Matchers {

    public static void assertThatModelsAreEqual(final Object rawModel,
                                                final MetricFilter sdkModel) {
        assertThat(rawModel).isInstanceOf(ResourceModel.class);
        ResourceModel model = (ResourceModel)rawModel;
        assertThat(model.getFilterName()).isEqualTo(sdkModel.filterName());
        assertThat(model.getFilterPattern()).isEqualTo(sdkModel.filterPattern());
        assertThat(model.getLogGroupName()).isEqualTo(sdkModel.logGroupName());
        assertThat(model.getMetricTransformations()).isEqualTo(translateFromSDK(sdkModel.metricTransformations()));
    }
}
