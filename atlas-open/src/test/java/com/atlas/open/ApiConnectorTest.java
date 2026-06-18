package com.atlas.open.connector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ApiConnector 连接器测试")
class ApiConnectorTest {

    private final ApiConnector connector = new ApiConnector();

    // ================== 鉴权头构建 ==================

    @Test
    @DisplayName("BASIC 应生成 Base64 编码的 Authorization 头")
    void should_build_basic_auth_header() {
        Map<String, String> credentials = Map.of("username", "admin", "password", "12345");
        Map<String, String> headers = connector.buildAuthHeaders("BASIC", credentials);

        assertThat(headers).containsKey("Authorization");
        assertThat(headers.get("Authorization")).startsWith("Basic ");
        assertThat(headers).containsEntry("Content-Type", "application/json");
    }

    @Test
    @DisplayName("BEARER 应生成 Bearer Authorization 头")
    void should_build_bearer_auth_header() {
        Map<String, String> credentials = Map.of("token", "my-jwt-token");
        Map<String, String> headers = connector.buildAuthHeaders("BEARER", credentials);

        assertThat(headers.get("Authorization")).isEqualTo("Bearer my-jwt-token");
    }

    @Test
    @DisplayName("API_KEY 应生成 X-API-Key 头")
    void should_build_api_key_header() {
        Map<String, String> credentials = Map.of("apiKey", "sk-secret-key");
        Map<String, String> headers = connector.buildAuthHeaders("API_KEY", credentials);

        assertThat(headers.get("X-API-Key")).isEqualTo("sk-secret-key");
        assertThat(headers).doesNotContainKey("Authorization");
    }

    @Test
    @DisplayName("OAUTH2 应生成 Bearer accessToken 头")
    void should_build_oauth2_header() {
        Map<String, String> credentials = Map.of("accessToken", "oauth-access-token");
        Map<String, String> headers = connector.buildAuthHeaders("OAUTH2", credentials);

        assertThat(headers.get("Authorization")).isEqualTo("Bearer oauth-access-token");
    }

    @Test
    @DisplayName("HMAC 应生成签名和时间戳头")
    void should_build_hmac_headers() {
        Map<String, String> credentials = Map.of(
                "signature", "sig-abc123",
                "timestamp", "1718123456"
        );
        Map<String, String> headers = connector.buildAuthHeaders("HMAC", credentials);

        assertThat(headers.get("X-HMAC-Signature")).isEqualTo("sig-abc123");
        assertThat(headers.get("X-HMAC-Timestamp")).isEqualTo("1718123456");
    }

    @Test
    @DisplayName("BASIC 缺少 credentials 字段时应使用空字符串")
    void should_use_empty_string_when_basic_credentials_missing() {
        Map<String, String> headers = connector.buildAuthHeaders("BASIC", new HashMap<>());

        assertThat(headers.get("Authorization")).startsWith("Basic ");
    }

    @Test
    @DisplayName("不支持的鉴权类型应抛 IllegalArgumentException")
    void should_throw_for_unsupported_auth_type() {
        assertThatThrownBy(() -> connector.buildAuthHeaders("DIGEST", Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("不支持的鉴权类型");
    }

    // ================== 请求体构建 ==================

    @Test
    @DisplayName("应替换模板中的变量占位符")
    void should_replace_variable_placeholders() {
        Map<String, String> variables = Map.of("name", "张三", "amount", "5000");
        String result = connector.buildRequestBody(
                "{\"user\":\"{{name}}\",\"amount\":{{amount}}}", variables);

        assertThat(result).isEqualTo("{\"user\":\"张三\",\"amount\":5000}");
    }

    @Test
    @DisplayName("多个相同变量应全部替换")
    void should_replace_all_occurrences_of_same_variable() {
        Map<String, String> variables = Map.of("id", "123");
        String result = connector.buildRequestBody(
                "{\"id\":\"{{id}}\",\"ref\":\"{{id}}\"}", variables);

        assertThat(result).isEqualTo("{\"id\":\"123\",\"ref\":\"123\"}");
    }

    @Test
    @DisplayName("null 模板应返回 {}")
    void should_return_empty_json_when_template_null() {
        String result = connector.buildRequestBody(null, Map.of("a", "b"));

        assertThat(result).isEqualTo("{}");
    }

    @Test
    @DisplayName("空模板应返回 {}")
    void should_return_empty_json_when_template_empty() {
        String result = connector.buildRequestBody("", Map.of("a", "b"));

        assertThat(result).isEqualTo("{}");
    }

    @Test
    @DisplayName("无匹配变量时模板应原样返回")
    void should_keep_template_unchanged_when_no_matching_variable() {
        String template = "{\"data\":\"{{missing}}\"}";
        String result = connector.buildRequestBody(template, Map.of());

        assertThat(result).isEqualTo(template);
    }

    @Test
    @DisplayName("大小写敏感的变量替换")
    void should_be_case_sensitive_for_variables() {
        Map<String, String> variables = Map.of("Name", "张三");
        String result = connector.buildRequestBody("{{Name}} {{name}}", variables);

        assertThat(result).isEqualTo("张三 {{name}}");
    }
}
