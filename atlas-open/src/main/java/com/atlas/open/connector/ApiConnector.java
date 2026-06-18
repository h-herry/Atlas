package com.atlas.open.connector;

import java.util.HashMap;
import java.util.Map;

/**
 * API 连接器 — 根据不同鉴权类型构建请求头与请求体
 */
public class ApiConnector {

    /**
     * 构建认证请求头
     */
    public Map<String, String> buildAuthHeaders(String authType, Map<String, String> credentials) {
        Map<String, String> headers = new HashMap<>();
        switch (authType.toUpperCase()) {
            case "BASIC" -> {
                String user = credentials.getOrDefault("username", "");
                String pass = credentials.getOrDefault("password", "");
                String encoded = java.util.Base64.getEncoder()
                        .encodeToString((user + ":" + pass).getBytes());
                headers.put("Authorization", "Basic " + encoded);
            }
            case "BEARER" -> {
                String token = credentials.getOrDefault("token", "");
                headers.put("Authorization", "Bearer " + token);
            }
            case "API_KEY" -> {
                headers.put("X-API-Key", credentials.getOrDefault("apiKey", ""));
            }
            case "OAUTH2" -> {
                headers.put("Authorization", "Bearer " + credentials.getOrDefault("accessToken", ""));
            }
            case "HMAC" -> {
                headers.put("X-HMAC-Signature", credentials.getOrDefault("signature", ""));
                headers.put("X-HMAC-Timestamp", credentials.getOrDefault("timestamp", ""));
            }
            default -> throw new IllegalArgumentException("不支持的鉴权类型: " + authType);
        }
        headers.put("Content-Type", "application/json");
        return headers;
    }

    /**
     * 构建请求体，支持变量替换 {{variable}}
     */
    public String buildRequestBody(String template, Map<String, String> variables) {
        if (template == null || template.isEmpty()) {
            return "{}";
        }
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
}
