package com.atlas.open.service;

import cn.hutool.crypto.digest.HMac;
import cn.hutool.crypto.digest.HmacAlgorithm;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.open.entity.ApiCallLog;
import com.atlas.open.entity.OpenApiClient;
import com.atlas.open.entity.WebhookSubscription;
import com.atlas.open.mapper.ApiCallLogMapper;
import com.atlas.open.mapper.OpenApiClientMapper;
import com.atlas.open.mapper.WebhookSubscriptionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 开放 API 服务 — 提供 AppKey/Secret 鉴权、客户端管理、调用日志与 Webhook 订阅能力
 * Open API Service — provides AppKey/Secret authentication, client management, call logging, and webhook subscriptions
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenApiService extends ServiceImpl<OpenApiClientMapper, OpenApiClient> {

    private final OpenApiClientMapper clientMapper;
    private final WebhookSubscriptionMapper webhookMapper;
    private final ApiCallLogMapper logMapper;

    /** 签名有效期（5分钟） / Signature validity window (5 minutes) */
    private static final long TIMESTAMP_TOLERANCE_SECONDS = 300;

    // ==================== 客户端管理 / Client Management ====================

    /**
     * 注册 API 客户端 — 自动生成 AppKey/Secret 并持久化
     * Register an API client — auto-generate AppKey/Secret and persist
     *
     * @param clientName      客户端名称 / client name
     * @param accessApis      可访问的 API 列表 / accessible API list
     * @param rateLimitPerMin 每分钟速率限制 / rate limit per minute
     * @param expireDate      过期日期 / expiration date
     * @return 已注册的客户端实体 / registered client entity
     */
    @Transactional(rollbackFor = Exception.class)
    public OpenApiClient register(String clientName, String accessApis, Integer rateLimitPerMin, LocalDate expireDate) {
        OpenApiClient client = new OpenApiClient();
        client.setClientName(clientName);
        client.setAppKey(generateKey());
        client.setAppSecret(generateSecret());
        client.setAccessApis(accessApis);
        client.setRateLimitPerMin(rateLimitPerMin != null ? rateLimitPerMin : 100);
        client.setExpireDate(expireDate);
        client.setStatus(1);
        save(client);
        return client;
    }

    /**
     * 分页查询 API 客户端列表
     * Paginated query of API client list
     *
     * @param page 当前页码 / current page number
     * @param size 每页大小 / page size
     * @return 分页结果 / paginated result
     */
    public Page<OpenApiClient> page(int page, int size) {
        return clientMapper.selectPage(new Page<>(page, size),
            new LambdaQueryWrapper<OpenApiClient>().orderByDesc(OpenApiClient::getCreatedAt));
    }

    /**
     * 禁用指定客户端（逻辑删除，将状态置为 0）
     * Disable a client by ID (soft delete: set status to 0)
     *
     * @param id 客户端ID / client ID
     * @return true-更新成功 / true if updated successfully
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean disableClient(Long id) {
        OpenApiClient client = new OpenApiClient();
        client.setId(id);
        client.setStatus(0);
        return updateById(client);
    }

    // ==================== 签名验证 / Signature Verification ====================

    /**
     * 验证 API 请求签名 — 校验时间戳防重放 + HMAC-SHA256 签名比对
     * Verify API request signature — validate timestamp for anti-replay + HMAC-SHA256 signature comparison
     *
     * @param request HTTP 请求对象 / HTTP request object
     * @return 验证通过则返回客户端信息 / returns the client entity if verification passes
     */
    public OpenApiClient verifySignature(HttpServletRequest request) {
        String appKey = request.getHeader("X-App-Key");
        String timestamp = request.getHeader("X-Timestamp");
        String signature = request.getHeader("X-Signature");

        if (appKey == null || timestamp == null || signature == null) {
            throw new BizException(ErrorCode.API_CLIENT_NOT_EXIST.getCode(), "缺少鉴权头: X-App-Key / X-Timestamp / X-Signature");
        }

        // 校验时间戳（防重放攻击） / Validate timestamp (anti-replay attack)
        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "X-Timestamp 格式无效");
        }
        if (Math.abs(System.currentTimeMillis() / 1000 - ts) > TIMESTAMP_TOLERANCE_SECONDS) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "签名已过期");
        }

        // 查找有效客户端 / Look up active client
        OpenApiClient client = clientMapper.selectOne(
            new LambdaQueryWrapper<OpenApiClient>()
                .eq(OpenApiClient::getAppKey, appKey)
                .eq(OpenApiClient::getStatus, 1)
        );
        if (client == null) {
            throw new BizException(ErrorCode.API_CLIENT_NOT_EXIST);
        }
        if (client.getExpireDate() != null && client.getExpireDate().isBefore(LocalDate.now())) {
            throw new BizException(ErrorCode.API_CLIENT_EXPIRED);
        }

        // HMAC-SHA256 签名计算 / HMAC-SHA256 signature computation
        String payload = appKey + ":" + timestamp + ":" + request.getRequestURI();
        HMac hmac = new HMac(HmacAlgorithm.HmacSHA256, client.getAppSecret().getBytes(StandardCharsets.UTF_8));
        String expected = hmac.digestHex(payload);

        if (!expected.equalsIgnoreCase(signature)) {
            throw new BizException(ErrorCode.API_SIGNATURE_INVALID);
        }

        return client;
    }

    // ==================== 调用日志 / Call Logs ====================

    /**
     * 记录 API 调用日志 — 异步写入，失败不影响主流程
     * Log an API call — asynchronous write, failure does not affect the main flow
     *
     * @param clientId       客户端ID / client ID
     * @param apiPath        API 路径 / API path
     * @param method         HTTP 方法 / HTTP method
     * @param requestIp      请求来源 IP / request source IP
     * @param responseStatus HTTP 响应状态码 / HTTP response status code
     * @param durationMs     调用耗时（毫秒） / call duration in milliseconds
     */
    public void logApiCall(Long clientId, String apiPath, String method, String requestIp,
                            Integer responseStatus, Long durationMs) {
        try {
            ApiCallLog logEntry = new ApiCallLog();
            logEntry.setClientId(clientId);
            logEntry.setApiPath(apiPath);
            logEntry.setMethod(method);
            logEntry.setRequestIp(requestIp);
            logEntry.setResponseStatus(responseStatus);
            logEntry.setDurationMs(durationMs);
            logEntry.setCreatedAt(LocalDateTime.now());
            logMapper.insert(logEntry);
        } catch (Exception e) {
            log.warn("记录API调用日志失败: {}", e.getMessage());
        }
    }

    /**
     * 分页查询 API 调用日志（可按客户端筛选）
     * Paginated query of API call logs (filterable by client)
     *
     * @param clientId 客户端ID（可选，传 null 则查全部） / client ID (optional, null for all)
     * @param page     当前页码 / current page number
     * @param size     每页大小 / page size
     * @return 分页结果 / paginated result
     */
    public Page<ApiCallLog> logPage(Long clientId, int page, int size) {
        LambdaQueryWrapper<ApiCallLog> wrapper = new LambdaQueryWrapper<>();
        if (clientId != null) wrapper.eq(ApiCallLog::getClientId, clientId);
        wrapper.orderByDesc(ApiCallLog::getCreatedAt);
        return logMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ==================== Webhook 管理 / Webhook Management ====================

    /**
     * 注册 Webhook 订阅 — 绑定事件类型、回调地址与签名密钥
     * Subscribe to a webhook event — bind event type, callback URL, and signing secret
     *
     * @param clientId    客户端ID / client ID
     * @param eventType   事件类型 / event type
     * @param callbackUrl 回调地址 / callback URL
     * @param secret      签名密钥 / signing secret
     * @return true-订阅成功 / true if subscription succeeded
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean subscribe(Long clientId, String eventType, String callbackUrl, String secret) {
        WebhookSubscription sub = new WebhookSubscription();
        sub.setClientId(clientId);
        sub.setEventType(eventType);
        sub.setCallbackUrl(callbackUrl);
        sub.setSecret(secret);
        sub.setStatus(1);
        return webhookMapper.insert(sub) > 0;
    }

    /**
     * 查询客户端的所有有效订阅
     * List all active subscriptions for a client
     *
     * @param clientId 客户端ID / client ID
     * @return 订阅列表 / subscription list
     */
    public List<WebhookSubscription> listSubscriptions(Long clientId) {
        return webhookMapper.selectList(
            new LambdaQueryWrapper<WebhookSubscription>()
                .eq(WebhookSubscription::getClientId, clientId)
                .eq(WebhookSubscription::getStatus, 1)
        );
    }

    /**
     * 取消指定 Webhook 订阅（逻辑删除，将状态置为 0）
     * Unsubscribe a webhook by ID (soft delete: set status to 0)
     *
     * @param id 订阅ID / subscription ID
     * @return true-取消成功 / true if unsubscribed successfully
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean unsubscribe(Long id) {
        WebhookSubscription sub = new WebhookSubscription();
        sub.setId(id);
        sub.setStatus(0);
        return webhookMapper.updateById(sub) > 0;
    }

    // ==================== 内部工具方法 / Internal Utilities ====================

    /**
     * 生成 AppKey：前缀 AK_ + 16 位大写 UUID 片段
     * Generate AppKey: prefix AK_ + 16-character uppercase UUID segment
     */
    private String generateKey() {
        return "AK_" + cn.hutool.core.util.IdUtil.fastSimpleUUID().substring(0, 16).toUpperCase();
    }

    /**
     * 生成 AppSecret：两段 UUID 拼接
     * Generate AppSecret: concatenation of two UUID segments
     */
    private String generateSecret() {
        return cn.hutool.core.util.IdUtil.fastSimpleUUID() + cn.hutool.core.util.IdUtil.fastSimpleUUID();
    }
}
