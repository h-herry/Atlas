package com.atlas.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * API 防重放攻击全局过滤器 — 校验 X-Nonce + X-Timestamp + X-Sign 请求头 /
 * API anti-replay attack global filter — validates X-Nonce + X-Timestamp + X-Sign headers
 *
 * <p>校验规则 / Validation rules:
 * <ol>
 *   <li>时间戳必须在 5 分钟窗口内 / Timestamp must be within 5-minute window</li>
 *   <li>nonce 存入 Redis（5分钟 TTL），重复 nonce 拒绝 / Nonce stored in Redis (5-min TTL), duplicate rejected</li>
 *   <li>签名算法: SHA256(timestamp + nonce + secret + body) / Sign algo: SHA256(timestamp + nonce + secret + body)</li>
 * </ol>
 *
 * <p>受保护路径 / Protected paths: /api/** /portal/**
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Component
public class NonceFilter implements GlobalFilter, Ordered {

    // 共享密钥（生产环境应从配置中心拉取）/ Shared secret (should be pulled from config center in production)
    private static final String SHARED_SECRET = "Atlas-Anti-Replay-Secret-V2";

    // 时间戳有效窗口（毫秒）/ Timestamp valid window (ms)
    private static final long TIMESTAMP_WINDOW_MS = 5 * 60 * 1000;

    // 不需要验证的路径前缀白名单 / Whitelist paths not requiring validation
    private static final String[] WHITELIST_PREFIXES = {
            "/api/user/login",
            "/api/user/logout",
            "/api/auth/",
            "/actuator/",
            "/doc.html",
            "/swagger",
            "/v3/api-docs"
    };

    private final NonceService nonceService;

    public NonceFilter(NonceService nonceService) {
        this.nonceService = nonceService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 非 API 请求跳过 / Skip non-API requests
        if (!path.startsWith("/api/") && !path.startsWith("/portal/")) {
            return chain.filter(exchange);
        }

        // 白名单路径跳过 / Skip whitelisted paths
        for (String prefix : WHITELIST_PREFIXES) {
            if (path.startsWith(prefix)) {
                return chain.filter(exchange);
            }
        }

        HttpHeaders headers = exchange.getRequest().getHeaders();
        String nonce = headers.getFirst("X-Nonce");
        String timestamp = headers.getFirst("X-Timestamp");

        // 请求头缺失 / Missing required headers
        if (!StringUtils.hasText(nonce) || !StringUtils.hasText(timestamp)) {
            log.warn("[ANTI-REPLAY] 请求头缺失 nonce={} ts={} path={}", nonce, timestamp, path);
            return writeError(exchange, 400, "请求缺少防重放参数 X-Nonce / X-Timestamp");
        }

        // 校验时间戳有效性（5分钟内）/ Validate timestamp validity (within 5 min)
        try {
            long ts = Long.parseLong(timestamp);
            long now = System.currentTimeMillis();
            if (Math.abs(now - ts) > TIMESTAMP_WINDOW_MS) {
                log.warn("[ANTI-REPLAY] 时间戳过期 ts={} now={} diff={}ms path={}",
                        ts, now, Math.abs(now - ts), path);
                return writeError(exchange, 400, "请求时间戳已过期，请校准客户端时间 / Timestamp expired, sync client clock");
            }
        } catch (NumberFormatException e) {
            return writeError(exchange, 400, "X-Timestamp 格式无效，需为 Unix 毫秒时间戳");
        }

        // 校验 nonce 防重（Redis UNIQUE 约束）/ Validate nonce uniqueness (Redis)
        boolean valid = nonceService.tryAcquire(nonce);
        if (!valid) {
            log.warn("[ANTI-REPLAY] nonce 重复 nonce={} path={}", nonce, path);
            return writeError(exchange, 429, "请求已处理或重复提交，请勿重放 / Request already processed, do not replay");
        }

        // 对 GET/DELETE/HEAD 等无 body 请求，直接验证无 body 签名后放行
        // For GET/DELETE/HEAD requests without body, validate sign with empty body and pass through
        HttpMethod method = exchange.getRequest().getMethod();
        if (method == HttpMethod.GET || method == HttpMethod.DELETE || method == HttpMethod.HEAD) {
            return validateSignAndContinue(exchange, chain, timestamp, nonce, "");
        }

        // 对有 body 的请求，缓存 body 用于签名验证后重新封装传递给下游
        // For requests with body, cache body for sign verification then re-wrap for downstream
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {
                    byte[] bodyBytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bodyBytes);
                    DataBufferUtils.release(dataBuffer);
                    String body = new String(bodyBytes, StandardCharsets.UTF_8);

                    // 签名校验 / Sign verification
                    String clientSign = headers.getFirst("X-Sign");
                    String expectedSign = computeSign(timestamp, nonce, body);
                    if (clientSign == null || !MessageDigest.isEqual(
                            clientSign.getBytes(StandardCharsets.UTF_8),
                            expectedSign.getBytes(StandardCharsets.UTF_8))) {
                        log.warn("[ANTI-REPLAY] 签名校验失败 path={}", path);
                        return writeError(exchange, 403, "签名校验失败 / Signature verification failed");
                    }

                    // 使用 ServerHttpRequestDecorator 重新封装请求体传递给下游
                    // Re-wrap request body using ServerHttpRequestDecorator for downstream
                    ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                        @Override
                        public HttpHeaders getHeaders() {
                            HttpHeaders mutatedHeaders = new HttpHeaders();
                            mutatedHeaders.putAll(super.getHeaders());
                            mutatedHeaders.set("X-Gateway-Anti-Replay", "PASSED");
                            return mutatedHeaders;
                        }

                        @Override
                        public Flux<DataBuffer> getBody() {
                            return Flux.just(new DefaultDataBufferFactory().wrap(bodyBytes));
                        }
                    };

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .switchIfEmpty(
                    // 无 body 请求也放行 / No body, pass through
                    validateSignAndContinue(exchange, chain, timestamp, nonce, "")
                );
    }

    /**
     * 签名校验后继续过滤链 / Continue filter chain after sign validation
     */
    private Mono<Void> validateSignAndContinue(ServerWebExchange exchange, GatewayFilterChain chain,
                                                String timestamp, String nonce, String body) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String clientSign = headers.getFirst("X-Sign");
        String expectedSign = computeSign(timestamp, nonce, body);
        if (clientSign == null || !MessageDigest.isEqual(
                clientSign.getBytes(StandardCharsets.UTF_8),
                expectedSign.getBytes(StandardCharsets.UTF_8))) {
            log.warn("[ANTI-REPLAY] 签名校验失败 path={}", exchange.getRequest().getURI().getPath());
            return writeError(exchange, 403, "签名校验失败 / Signature verification failed");
        }
        return chain.filter(exchange);
    }

    /**
     * 计算签名: SHA256(timestamp + nonce + secret + body) / Compute sign
     */
    private String computeSign(String timestamp, String nonce, String body) {
        try {
            String plain = timestamp + nonce + SHARED_SECRET + (body != null ? body : "");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            log.error("[ANTI-REPLAY] 签名计算异常", e);
            return "";
        }
    }

    private Mono<Void> writeError(ServerWebExchange exchange, int status, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.valueOf(status));
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"code\":%d,\"message\":\"%s\",\"data\":null}", status, message);
        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // 在安全过滤器之后、路由转发之前执行 / Execute after security filter, before routing
        return -20;
    }
}
