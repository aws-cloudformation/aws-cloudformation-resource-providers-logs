package software.amazon.logs.metricfilter;

import software.amazon.awssdk.services.cloudwatchlogs.model.MetricFilter;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class Matchers {

    public static void assertThatModelsAreEqual(final Object rawModel,
                                                final MetricFilter sdkModel) {
        assertThat(rawModel).isInstanceOf(ResourceModel.class);
        ResourceModel model = (ResourceModel)rawModel;
        assertThat(model.getFilterName()).isEqualTo(sdkModel.filterName());
        assertThat(model.getFilterPattern()).isEqualTo(sdkModel.filterPattern());
        assertThat(model.getLogGroupName()).isEqualTo(sdkModel.logGroupName());

        List<MetricTransformation> mts = sdkModel.metricTransformations()
                .stream()
                .map(Translator::translateMetricTransformationFromSdk)
                .collect(Collectors.toList());
        assertThat(model.getMetricTransformations()).isEqualTo(mts);
    }
}
