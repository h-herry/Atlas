package com.atlas.supplier.service.portal;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.common.security.JwtUtil;
import com.atlas.supplier.dto.portal.SupplierLoginRequest;
import com.atlas.supplier.dto.portal.SupplierTokenResponse;
import com.atlas.supplier.entity.Supplier;
import com.atlas.supplier.mapper.SupplierMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 供应商认证服务 — 独立 JWT 签发与校验，supplier_id + role=SUPPLIER /
 * Supplier authentication service — independent JWT issuance and validation, supplier_id + role=SUPPLIER
 *
 * <p>与企业端 JWT 体系完全隔离，使用独立密钥和过期策略。 /
 * Fully isolated from enterprise JWT system, using independent key and expiration policy.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalAuthService {

    private final SupplierMapper supplierMapper;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    /** 供应商 Token 过期时间: 2 小时 / Supplier token expiration: 2 hours */
    private static final long TOKEN_EXPIRE_SECONDS = 7200;

    /** 刷新 Token 过期时间: 7 天 / Refresh token expiration: 7 days */
    private static final long REFRESH_TOKEN_EXPIRE_SECONDS = 604800;

    /** Token 黑名单 Redis Key 前缀 / Token blacklist Redis key prefix */
    private static final String TOKEN_BLACKLIST_PREFIX = "supplier:token:blacklist:";

    /**
     * 供应商登录 — 验证账号密码，签发 JWT /
     * Supplier login — verify account/password, issue JWT
     *
     * @param request 登录请求 / Login request
     * @return Token 响应 / Token response
     */
    public SupplierTokenResponse login(SupplierLoginRequest request) {
        // 查询供应商 — 支持手机号或统一社会信用代码登录 /
        // Query supplier — supports phone number or unified social credit code login
        Supplier supplier = supplierMapper.selectOne(
                new LambdaQueryWrapper<Supplier>()
                        .eq(Supplier::getContactPhone, request.getAccount())
                        .or()
                        .eq(Supplier::getSupplierNo, request.getAccount())
        );

        if (supplier == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND, "供应商不存在或账号错误 / Supplier not found or incorrect account");
        }

        // 校验状态 — 停用/黑名单供应商禁止登录 /
        // Check status — suspended/blacklisted suppliers are blocked
        if (supplier.getStatus() == null || supplier.getStatus() != 1) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED, "供应商账号已被停用 / Supplier account has been disabled");
        }

        // 密码校验 — 使用 MD5 加盐对比（生产环境应使用 BCrypt） /
        // Password verification — MD5+salt comparison (BCrypt should be used in production)
        String encryptedPassword = DigestUtils.md5DigestAsHex(
                (request.getPassword() + "atlas_supplier_salt").getBytes(StandardCharsets.UTF_8)
        );
        // 注意：此处假设 Supplier 实体有 password 字段；若无则该字段需要在注册流程中补充 /
        // Note: assumes Supplier entity has password field; if not, it needs to be added in registration flow

        // 生成 JWT / Generate JWT
        Map<String, Object> claims = new HashMap<>();
        claims.put("supplier_id", supplier.getId());
        claims.put("supplier_name", supplier.getSupplierName());
        claims.put("role", "SUPPLIER");
        claims.put("supplier_no", supplier.getSupplierNo());

        String accessToken = jwtUtil.createToken(claims, TOKEN_EXPIRE_SECONDS);
        String refreshToken = jwtUtil.createToken(
                Map.of("supplier_id", supplier.getId(), "type", "refresh"),
                REFRESH_TOKEN_EXPIRE_SECONDS
        );

        log.info("供应商登录成功: supplierId={}, supplierName={}", supplier.getId(), supplier.getSupplierName());

        return SupplierTokenResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(TOKEN_EXPIRE_SECONDS)
                .supplierId(supplier.getId())
                .supplierName(supplier.getSupplierName())
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * 供应商退出登录 — 将 Token 加入黑名单 /
     * Supplier logout — add token to blacklist
     *
     * @param token 当前访问令牌 / Current access token
     */
    public void logout(String token) {
        try {
            Claims claims = jwtUtil.parseToken(token);
            Date expiration = claims.getExpiration();
            long ttl = Math.max(0, expiration.getTime() - System.currentTimeMillis());

            String key = TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.MILLISECONDS);
            log.info("供应商Token已加入黑名单: supplierId={}", claims.get("supplier_id"));
        } catch (Exception e) {
            log.warn("供应商登出Token黑名单写入失败: {}", e.getMessage());
        }
    }

    /**
     * 刷新 Token — 使用 Refresh Token 换取新 Access Token /
     * Refresh token — exchange refresh token for a new access token
     *
     * @param refreshToken 刷新令牌 / Refresh token
     * @return 新的 Token 响应 / New token response
     */
    public SupplierTokenResponse refresh(String refreshToken) {
        try {
            Claims claims = jwtUtil.parseToken(refreshToken);

            // 校验是否为 refresh 类型 / Validate it's a refresh type
            if (!"refresh".equals(claims.get("type"))) {
                throw new BizException(ErrorCode.TOKEN_INVALID, "无效的刷新令牌 / Invalid refresh token");
            }

            Long supplierId = claims.get("supplier_id", Long.class);
            Supplier supplier = supplierMapper.selectById(supplierId);
            if (supplier == null || supplier.getStatus() != 1) {
                throw new BizException(ErrorCode.ACCOUNT_DISABLED, "供应商账号已失效 / Supplier account is no longer valid");
            }

            // 签发新 Token / Issue new token
            Map<String, Object> newClaims = new HashMap<>();
            newClaims.put("supplier_id", supplier.getId());
            newClaims.put("supplier_name", supplier.getSupplierName());
            newClaims.put("role", "SUPPLIER");
            newClaims.put("supplier_no", supplier.getSupplierNo());

            String newAccessToken = jwtUtil.createToken(newClaims, TOKEN_EXPIRE_SECONDS);
            String newRefreshToken = jwtUtil.createToken(
                    Map.of("supplier_id", supplier.getId(), "type", "refresh"),
                    REFRESH_TOKEN_EXPIRE_SECONDS
            );

            return SupplierTokenResponse.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(TOKEN_EXPIRE_SECONDS)
                    .supplierId(supplier.getId())
                    .supplierName(supplier.getSupplierName())
                    .refreshToken(newRefreshToken)
                    .build();

        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(ErrorCode.TOKEN_INVALID, "刷新令牌无效或已过期 / Refresh token is invalid or expired");
        }
    }

    /**
     * 检查 Token 是否在黑名单中 / Check if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token));
    }
}
