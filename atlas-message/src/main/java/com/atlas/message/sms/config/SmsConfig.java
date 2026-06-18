package com.atlas.message.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 短信配置 — 读取 application.yml 中 sms.* 配置项 /
 * SMS configuration — reads sms.* configuration items from application.yml
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sms")
public class SmsConfig {

    /** 是否启用短信通道 / Whether SMS channel is enabled */
    private boolean enabled = false;

    /** 短信提供商: aliyun / tencent / custom / SMS provider: aliyun / tencent / custom */
    private String provider = "aliyun";

    /** 阿里云短信配置 / Alibaba Cloud SMS configuration */
    private AliyunConfig aliyun = new AliyunConfig();

    /** 腾讯云短信配置（预留）/ Tencent Cloud SMS configuration (reserved) */
    private TencentConfig tencent = new TencentConfig();

    @Data
    public static class AliyunConfig {
        /** 阿里云 AccessKey ID / Alibaba Cloud AccessKey ID */
        private String accessKeyId;

        /** 阿里云 AccessKey Secret / Alibaba Cloud AccessKey Secret */
        private String accessKeySecret;

        /** 短信签名 / SMS sign name */
        private String signName;

        /** 模板编码映射（业务场景 → 阿里云模板CODE）/ Template code mapping (business scenario → Alibaba Cloud template CODE) */
        private Map<String, String> templateCodes;
    }

    @Data
    public static class TencentConfig {
        /** 腾讯云 SDK App ID / Tencent Cloud SDK App ID */
        private String appId;

        /** 腾讯云 App Key / Tencent Cloud App Key */
        private String appKey;

        /** 短信签名 / SMS sign name */
        private String signName;
    }
}
