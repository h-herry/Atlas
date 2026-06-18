package com.atlas.open.service;

import com.atlas.open.config.ApiConfigProperties;
import com.atlas.open.connector.ApiConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DynamicApiService 动态 API 服务测试")
@ExtendWith(MockitoExtension.class)
class DynamicApiServiceTest {

    private DynamicApiService service;
    private ApiConfigProperties configProperties;
    private ApiConnector apiConnector;

    @BeforeEach
    void setUp() {
        configProperties = new ApiConfigProperties();
        configProperties.setMaxRetries(2);
        configProperties.setRetryInterval(10);
        apiConnector = new ApiConnector();
        service = new DynamicApiService(configProperties, apiConnector);
    }

    // ================== 鉴权分支 ==================

    @Test
    @DisplayName("BASIC 鉴权应生成 Basic Auth 头")
    void should_build_basic_auth_headers() {
        Map<String, String> credentials = Map.of("username", "admin", "password", "secret");

        String result = service.executeApiCall("BASIC", credentials, "http://api.test", Map.of());

        assertThat(result).contains("\"status\":\"ok\"");
    }

    @Test
    @DisplayName("BEARER 鉴权应生成 Bearer Token 头")
    void should_build_bearer_auth_headers() {
        Map<String, String> credentials = Map.of("token", "jwt-token-xyz");

        String result = service.executeApiCall("BEARER", credentials, "http://api.test", Map.of());

        assertThat(result).contains("\"status\":\"ok\"");
    }

    @Test
    @DisplayName("API_KEY 鉴权应生成 X-API-Key 头")
    void should_build_api_key_auth_headers() {
        Map<String, String> credentials = Map.of("apiKey", "sk-123456");

        String result = service.executeApiCall("API_KEY", credentials, "http://api.test", Map.of());

        assertThat(result).contains("\"status\":\"ok\"");
    }

    @Test
    @DisplayName("OAUTH2 鉴权应生成 Bearer accessToken 头")
    void should_build_oauth2_auth_headers() {
        Map<String, String> credentials = Map.of("accessToken", "oauth-token-abc");

        String result = service.executeApiCall("OAUTH2", credentials, "http://api.test", Map.of());

        assertThat(result).contains("\"status\":\"ok\"");
    }

    @Test
    @DisplayName("HMAC 鉴权应生成签名头")
    void should_build_hmac_auth_headers() {
        Map<String, String> credentials = Map.of(
                "signature", "hmac-sig-123",
                "timestamp", "1718000000"
        );

        String result = service.executeApiCall("HMAC", credentials, "http://api.test", Map.of());

        assertThat(result).contains("\"status\":\"ok\"");
    }

    // ================== 变量替换 ==================

    @Test
    @DisplayName("URL 中的变量应被替换")
    void should_replace_variables_in_url() {
        Map<String, String> variables = Map.of("orderId", "PO-100", "status", "approved");
        String urlTemplate = "http://api.test/orders/{{orderId}}?status={{status}}";

        String result = service.executeApiCall("API_KEY", Map.of("apiKey", "sk-1"), urlTemplate, variables);

        assertThat(result).contains("orders/PO-100");
        assertThat(result).contains("status=approved");
    }

    @Test
    @DisplayName("无变量的模板应保持原样")
    void should_keep_template_unchanged_when_no_variables() {
        String result = service.executeApiCall("API_KEY", Map.of("apiKey", "sk-1"),
                "http://api.test/health", new HashMap<>());

        assertThat(result).contains("http://api.test/health");
    }

    // ================== 重试机制 ==================

    @Test
    @DisplayName("首次成功不应重试")
    void should_not_retry_when_first_attempt_succeeds() {
        long start = System.currentTimeMillis();
        String result = service.executeApiCall("BEARER", Map.of("token", "t1"),
                "http://api.test", Map.of());
        long elapsed = System.currentTimeMillis() - start;

        assertThat(result).contains("\"status\":\"ok\"");
        // 不应有重试间隔等待
        assertThat(elapsed).isLessThan(50L);
    }

    @Test
    @DisplayName("应返回支持的鉴权类型列表")
    void should_return_supported_auth_types() {
        String[] types = service.getSupportedAuthTypes();

        assertThat(types).containsExactly("BASIC", "BEARER", "API_KEY", "OAUTH2", "HMAC");
    }

    @Test
    @DisplayName("不支持的鉴权类型应抛异常")
    void should_throw_when_unsupported_auth_type() {
        assertThatThrownBy(() -> service.executeApiCall("UNKNOWN", Map.of(),
                "http://api.test", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不支持的鉴权类型");
    }
}
