package software.amazon.logs.loggroup;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationTest {

    @Test
    public void testResourceDefinedTags_MergeDuplicateKeys() {
        final Set<Tag> tags = new HashSet<>(Arrays.asList(
                Tag.builder().key("key-1").value("value-1").build(),
                Tag.builder().key("key-1").value("value-2").build()
        ));
        final ResourceModel model = ResourceModel.builder()
                .tags(tags)
                .build();

        final Configuration configuration = new Configuration();

        assertThat(configuration.resourceDefinedTags(model)).isEqualTo(Collections.singletonMap("key-1", "value-2"));
    }
}
