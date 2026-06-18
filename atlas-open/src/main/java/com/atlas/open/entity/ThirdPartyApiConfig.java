package com.atlas.open.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 第三方 API 配置 / Third-party API configuration
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("third_party_api_config")
public class ThirdPartyApiConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置编码 / Config code */
    private String configCode;

    /** 配置名称 / Config name */
    private String configName;

    /** 第三方平台名称 / Third-party platform name */
    private String platformName;

    /** API基础URL / API base URL */
    private String baseUrl;

    /** 鉴权方式: NONE/BASIC/BEARER/API_KEY/OAUTH2/HMAC / Authentication type */
    private String authType;

    /** 鉴权配置JSON / Auth config JSON */
    private String authConfig;

    /** 默认请求头JSON / Default headers JSON */
    private String headers;

    /** 超时时间（毫秒） / Timeout (ms) */
    private Integer timeoutMs;

    /** 重试次数 / Retry count */
    private Integer retryCount;

    /** 状态: 1启用 0禁用 / Status: 1-enabled 0-disabled */
    private Integer status;

    /** 创建时间 / Creation time */
    private LocalDateTime createdAt;
}
