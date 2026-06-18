package com.atlas.open.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 开放 API 客户端（AppKey/Secret 鉴权） / Open API client (AppKey/Secret authentication)
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("open_api_client")
public class OpenApiClient {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 客户端名称 / Client name */
    private String clientName;

    /** AppKey / AppKey */
    private String appKey;

    /** AppSecret（HMAC-SHA256 签名密钥） / AppSecret (HMAC-SHA256 signing key) */
    private String appSecret;

    /** 授权 API 列表（逗号分隔） / Authorized API list (comma-separated) */
    private String accessApis;

    /** 每分钟请求限流 / Rate limit per minute */
    private Integer rateLimitPerMin;

    /** 状态: 1启用 0禁用 / Status: 1-enabled 0-disabled */
    private Integer status;

    /** 有效期 / Expiration date */
    private LocalDate expireDate;

    /** 创建时间 / Creation time */
    private LocalDateTime createdAt;
}
