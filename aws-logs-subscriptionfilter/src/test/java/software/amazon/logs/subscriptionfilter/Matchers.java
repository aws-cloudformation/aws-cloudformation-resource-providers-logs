package software.amazon.logs.subscriptionfilter;

import software.amazon.awssdk.services.cloudwatchlogs.model.SubscriptionFilter;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class Matchers {

    public static void assertThatModelsAreEqual(final Object rawModel,
                                                final SubscriptionFilter sdkModel) {
        assertThat(rawModel).isInstanceOf(ResourceModel.class);
        ResourceModel model = (ResourceModel)rawModel;
        assertThat(model.getRoleArn()).isEqualTo(sdkModel.roleArn());
        assertThat(model.getFilterPattern()).isEqualTo(sdkModel.filterPattern());
        assertThat(model.getLogGroupName()).isEqualTo(sdkModel.logGroupName());
        assertThat(model.getDestinationArn()).isEqualTo(sdkModel.destinationArn());
    }
}