package com.aws.logs.metricfilter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class TranslatorTest {

    @Test
    public void translate_nullPackageModel_returnsNull() {
        assertThat(Translator.translate((MetricTransformation)null)).isNull();
    }

    @Test
    public void translate_nullSDKModel_returnsNull() {
        assertThat(Translator.translate((software.amazon.awssdk.services.cloudwatchlogs.model.MetricTransformation)null)).isNull();
    }

    @Test
    public void translateToSDK_emptyList_returnsNull() {
        assertThat(Translator.translateToSDK(Collections.emptyList())).isNull();
    }

    @Test
    public void translateFromSDK_emptyList_returnsNull() {
        assertThat(Translator.translateFromSDK(Collections.emptyList())).isNull();
    }
}
