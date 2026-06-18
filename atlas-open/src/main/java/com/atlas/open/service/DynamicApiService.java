package com.atlas.open.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.open.entity.ApiEndpointConfig;
import com.atlas.open.entity.ApiIntegrationLog;
import com.atlas.open.entity.ThirdPartyApiConfig;
import com.atlas.open.mapper.ApiEndpointConfigMapper;
import com.atlas.open.mapper.ApiIntegrationLogMapper;
import com.atlas.open.mapper.ThirdPartyApiConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 动态 API 调用引擎
 * <p>
 * 核心能力：
 * <ul>
 *   <li>读取 third_party_api_config + api_endpoint_config 配置</li>
 *   <li>StringTemplate 变量占位符替换 {{xxx}}</li>
 *   <li>多鉴权方式: BASIC / BEARER / API_KEY / OAUTH2 / HMAC-SHA256</li>
 *   <li>JSONPath 响应映射</li>
 *   <li>指数退避重试 + 调用日志记录</li>
 * </ul>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicApiService {

    private final ThirdPartyApiConfigMapper configMapper;
    private final ApiEndpointConfigMapper endpointMapper;
    private final ApiIntegrationLogMapper logMapper;
    private final RestTemplate restTemplate;

    /** 变量占位符模式: {{variableName}} */
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    // ==================== 公开入口 / Public Entry ====================

    /**
     * 按 endpointCode 动态执行 API 调用
     *
     * @param endpointCode 接口编码
     * @param params       请求参数（变量替换 + URL query 参数）
     * @return 响应映射后的结果
     */
    @SuppressWarnings("unchecked")
    public Object execute(String endpointCode, Map<String, Object> params) {
        // 1. 加载配置
        ApiEndpointConfig endpoint = endpointMapper.selectOne(
                new LambdaQueryWrapper<ApiEndpointConfig>()
                        .eq(ApiEndpointConfig::getEndpointCode, endpointCode)
                        .eq(ApiEndpointConfig::getStatus, 1));
        if (endpoint == null) {
            throw new BizException(ErrorCode.NOT_FOUND.getCode(), "接口配置不存在: " + endpointCode);
        }

        ThirdPartyApiConfig config = configMapper.selectById(endpoint.getConfigId());
        if (config == null || config.getStatus() != 1) {
            throw new BizException(ErrorCode.NOT_FOUND.getCode(), "第三方API配置不存在或已禁用");
        }

        // 2. 构建完整 URL
        String fullUrl = config.getBaseUrl() + endpoint.getPath();

        // 3. 模板变量替换
        String requestBody = replaceVariables(endpoint.getRequestTemplate(), params);

        // 4. 构建请求头（含鉴权）
        HttpHeaders headers = buildHeaders(config);

        // 5. 带重试的 HTTP 调用
        int maxRetries = config.getRetryCount() != null ? config.getRetryCount() : 3;
        long timeoutMs = config.getTimeoutMs() != null ? config.getTimeoutMs() : 10000;

        ApiCallResult callResult = executeWithRetry(
                endpoint.getMethod(), fullUrl, requestBody, headers, maxRetries, timeoutMs);

        // 6. 记录日志
        saveLog(config.getId(), endpointCode, fullUrl, requestBody, callResult);

        // 7. 成功判断
        if (!callResult.success) {
            throw new BizException(ErrorCode.INTERNAL_ERROR.getCode(),
                    "第三方API调用失败: " + callResult.errorMsg);
        }

        // 8. 响应映射
        if (StrUtil.isNotBlank(endpoint.getResponseMapping())) {
            return applyResponseMapping(callResult.responseBody, endpoint.getResponseMapping());
        }

        // 默认返回原始响应
        try {
            return JSONUtil.parse(callResult.responseBody);
        } catch (Exception e) {
            return callResult.responseBody;
        }
    }

    // ==================== 变量替换 / Variable Substitution ====================

    /**
     * 将模板中的 {{varName}} 替换为 params 中的值
     */
    String replaceVariables(String template, Map<String, Object> params) {
        if (StrUtil.isBlank(template) || params == null || params.isEmpty()) {
            return template;
        }
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = params.get(varName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    // ==================== 鉴权 / Authentication ====================

    /**
     * 根据配置构建带鉴权的请求头
     */
    HttpHeaders buildHeaders(ThirdPartyApiConfig config) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 用户自定义默认 Header
        if (StrUtil.isNotBlank(config.getHeaders())) {
            JSONObject customHeaders = JSONUtil.parseObj(config.getHeaders());
            for (String key : customHeaders.keySet()) {
                headers.set(key, customHeaders.getStr(key));
            }
        }

        // 鉴权
        String authType = config.getAuthType() != null ? config.getAuthType().toUpperCase() : "NONE";
        JSONObject authJson = StrUtil.isNotBlank(config.getAuthConfig())
                ? JSONUtil.parseObj(config.getAuthConfig()) : new JSONObject();

        switch (authType) {
            case "BASIC":
                String basicUser = authJson.getStr("username", "");
                String basicPass = authJson.getStr("password", "");
                String basicAuth = java.util.Base64.getEncoder()
                        .encodeToString((basicUser + ":" + basicPass).getBytes(StandardCharsets.UTF_8));
                headers.set("Authorization", "Basic " + basicAuth);
                break;

            case "BEARER":
                String token = authJson.getStr("token", "");
                headers.set("Authorization", "Bearer " + token);
                break;

            case "API_KEY":
                String apiKey = authJson.getStr("api_key", "");
                String apiKeyHeader = authJson.getStr("header_name", "X-API-Key");
                headers.set(apiKeyHeader, apiKey);
                break;

            case "OAUTH2":
                // client_credentials 模式：先获取 token
                String tokenUrl = authJson.getStr("token_url");
                String clientId = authJson.getStr("client_id");
                String clientSecret = authJson.getStr("client_secret");
                if (StrUtil.isNotBlank(tokenUrl) && StrUtil.isNotBlank(clientId)) {
                    String oauthToken = fetchOAuth2Token(tokenUrl, clientId, clientSecret);
                    headers.set("Authorization", "Bearer " + oauthToken);
                }
                break;

            case "HMAC":
                // HMAC-SHA256 签名：将请求参数拼接后签名
                String hmacSecret = authJson.getStr("secret", "");
                long timestamp = System.currentTimeMillis() / 1000;
                String nonce = cn.hutool.core.util.IdUtil.fastSimpleUUID().substring(0, 16);
                String signPayload = timestamp + "\n" + nonce;
                HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, hmacSecret.getBytes(StandardCharsets.UTF_8));
                String signature = hmac.digestHex(signPayload);
                headers.set("X-Timestamp", String.valueOf(timestamp));
                headers.set("X-Nonce", nonce);
                headers.set("X-Signature", signature);
                break;

            case "NONE":
            default:
                break;
        }

        return headers;
    }

    private String fetchOAuth2Token(String tokenUrl, String clientId, String clientSecret) {
        try {
            HttpHeaders oauthHeaders = new HttpHeaders();
            oauthHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            String body = "grant_type=client_credentials&client_id=" + clientId
                    + "&client_secret=" + (clientSecret != null ? clientSecret : "");
            HttpEntity<String> entity = new HttpEntity<>(body, oauthHeaders);
            ResponseEntity<String> resp = restTemplate.postForEntity(tokenUrl, entity, String.class);
            if (resp.getBody() != null) {
                JSONObject tokenResp = JSONUtil.parseObj(resp.getBody());
                return tokenResp.getStr("access_token", "");
            }
        } catch (Exception e) {
            log.error("获取 OAuth2 Token 失败: url={}", tokenUrl, e);
        }
        return "";
    }

    // ==================== HTTP 调用 + 重试 / HTTP Call + Retry ====================

    /**
     * 带指数退避的 HTTP 调用
     */
    ApiCallResult executeWithRetry(String method, String url, String body,
                                   HttpHeaders headers, int maxRetries, long timeoutMs) {
        Exception lastException = null;
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                long start = System.currentTimeMillis();
                HttpEntity<String> entity = new HttpEntity<>(body, headers);
                ResponseEntity<String> response;

                HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
                response = restTemplate.exchange(url, httpMethod, entity, String.class);

                long duration = System.currentTimeMillis() - start;
                int statusCode = response.getStatusCode().value();

                // 判断成功条件
                boolean success = statusCode >= 200 && statusCode < 300;
                return new ApiCallResult(success, response.getBody(), statusCode, duration,
                        success ? null : "HTTP " + statusCode);
            } catch (RestClientException e) {
                lastException = e;
                log.warn("API调用失败: url={} attempt={}/{} error={}", url, attempt + 1, maxRetries, e.getMessage());
            }

            // 指数退避
            if (attempt < maxRetries - 1) {
                try {
                    long delay = 100L * (1L << attempt);
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        long duration = 0;
        return new ApiCallResult(false, null, 0, duration,
                lastException != null ? lastException.getMessage() : "达到最大重试次数");
    }

    // ==================== 响应映射 / Response Mapping ====================

    /**
     * JSONPath 响应映射
     * <p>配置示例: {"supplierList": "$.data.items[*].id"}
     */
    @SuppressWarnings("unchecked")
    Object applyResponseMapping(String responseBody, String mappingJson) {
        if (StrUtil.isBlank(responseBody) || StrUtil.isBlank(mappingJson)) {
            return responseBody;
        }
        try {
            Map<String, String> mapping = JSONUtil.toBean(mappingJson, Map.class);
            Configuration conf = Configuration.defaultConfiguration()
                    .addOptions(Option.SUPPRESS_EXCEPTIONS);
            DocumentContext ctx = JsonPath.using(conf).parse(responseBody);

            if (mapping.size() == 1 && mapping.values().iterator().next().contains("[*]")) {
                // 单字段列表映射：直接返回数组
                String jsonPath = mapping.values().iterator().next();
                return ctx.read(jsonPath);
            }

            // 多字段映射：构建新 JSON 对象
            JSONObject result = new JSONObject();
            for (Map.Entry<String, String> entry : mapping.entrySet()) {
                Object value = ctx.read(entry.getValue());
                result.set(entry.getKey(), value);
            }
            return result;
        } catch (Exception e) {
            log.error("响应映射失败: mapping={}", mappingJson, e);
            return JSONUtil.parse(responseBody);
        }
    }

    // ==================== 日志记录 / Log Recording ====================

    void saveLog(Long configId, String endpointCode, String requestUrl,
                 String requestBody, ApiCallResult result) {
        try {
            ApiIntegrationLog logEntry = new ApiIntegrationLog();
            logEntry.setConfigId(configId);
            logEntry.setEndpointCode(endpointCode);
            logEntry.setRequestUrl(requestUrl);
            logEntry.setRequestBody(requestBody);
            logEntry.setResponseBody(result.responseBody);
            logEntry.setResponseStatus(result.statusCode);
            logEntry.setDurationMs(result.durationMs);
            logEntry.setSuccess(result.success ? 1 : 0);
            logEntry.setErrorMsg(result.errorMsg);
            logEntry.setCreatedAt(LocalDateTime.now());
            logMapper.insert(logEntry);
        } catch (Exception e) {
            log.warn("记录API调用日志失败: {}", e.getMessage());
        }
    }

    // ==================== 查询接口 / Query APIs ====================

    /**
     * 查询调用日志
     */
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<ApiIntegrationLog> logPage(
            Long configId, int page, int size) {
        LambdaQueryWrapper<ApiIntegrationLog> wrapper = new LambdaQueryWrapper<>();
        if (configId != null) {
            wrapper.eq(ApiIntegrationLog::getConfigId, configId);
        }
        wrapper.orderByDesc(ApiIntegrationLog::getCreatedAt);
        return logMapper.selectPage(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), wrapper);
    }

    /**
     * 查询第三方API配置
     */
    public ThirdPartyApiConfig getConfig(Long id) {
        return configMapper.selectById(id);
    }

    /**
     * 分页查询第三方API配置
     */
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<ThirdPartyApiConfig> configPage(
            int page, int size) {
        return configMapper.selectPage(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size),
                new LambdaQueryWrapper<ThirdPartyApiConfig>().orderByDesc(ThirdPartyApiConfig::getCreatedAt));
    }

    /**
     * 查询接口定义
     */
    public java.util.List<ApiEndpointConfig> listEndpoints(Long configId) {
        return endpointMapper.selectList(
                new LambdaQueryWrapper<ApiEndpointConfig>()
                        .eq(ApiEndpointConfig::getConfigId, configId));
    }

    // ==================== 内部类 / Inner Class ====================

    @lombok.AllArgsConstructor
    static class ApiCallResult {
        boolean success;
        String responseBody;
        int statusCode;
        long durationMs;
        String errorMsg;
    }
}
