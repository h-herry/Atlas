package com.atlas.open.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiConfigProperties 配置绑定测试")
class ApiConfigPropertiesTest {

    @Test
    @DisplayName("默认值应正确")
    void should_have_correct_defaults() {
        ApiConfigProperties props = new ApiConfigProperties();

        assertThat(props.getDefaultAuthType()).isEqualTo("API_KEY");
        assertThat(props.getConnectTimeout()).isEqualTo(5000);
        assertThat(props.getReadTimeout()).isEqualTo(10000);
        assertThat(props.getMaxRetries()).isEqualTo(3);
        assertThat(props.getRetryInterval()).isEqualTo(1000);
        assertThat(props.getWebhookBaseUrl()).isEqualTo("http://localhost:8080/callback");
    }

    @Test
    @DisplayName("setter 应正确设置值")
    void should_set_values_via_setters() {
        ApiConfigProperties props = new ApiConfigProperties();
        props.setDefaultAuthType("OAUTH2");
        props.setConnectTimeout(3000);
        props.setReadTimeout(5000);
        props.setMaxRetries(5);
        props.setRetryInterval(500);
        props.setWebhookBaseUrl("https://webhook.example.com");

        assertThat(props.getDefaultAuthType()).isEqualTo("OAUTH2");
        assertThat(props.getConnectTimeout()).isEqualTo(3000);
        assertThat(props.getReadTimeout()).isEqualTo(5000);
        assertThat(props.getMaxRetries()).isEqualTo(5);
        assertThat(props.getRetryInterval()).isEqualTo(500);
        assertThat(props.getWebhookBaseUrl()).isEqualTo("https://webhook.example.com");
    }
}
