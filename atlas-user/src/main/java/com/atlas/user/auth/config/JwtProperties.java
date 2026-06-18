package com.atlas.user.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性 — 双通道（enterprise/supplier）使用不同 issuer /
 * JWT configuration properties — dual-channel with distinct issuers
 *
 * 配置前缀 / Config prefix: jwt
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** HMAC-SHA 签名密钥（Base64 编码） / HMAC-SHA signing key (Base64 encoded) */
    private String secret = "atlas-jwt-secret-key-default";

    /** Token 过期时间（秒），默认 7200s = 2h / Token expiration in seconds, default 7200s = 2h */
    private long expiration = 7200;

    /** 企业端 JWT 签发者 / Enterprise channel JWT issuer */
    private String enterpriseIssuer = "atlas-enterprise";

    /** 供应商端 JWT 签发者 / Supplier channel JWT issuer */
    private String supplierIssuer = "atlas-supplier";
}
