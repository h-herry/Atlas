package com.atlas.message.sms.provider;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dysmsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.sdk.service.dysmsapi20170525.models.SendSmsResponse;
import com.atlas.message.sms.config.SmsConfig;
import com.atlas.message.sms.dto.SmsSendResult;
import darabonba.core.client.ClientOverrideConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 阿里云短信实现 — 基于阿里云 dysmsapi SDK 发送短信 /
 * Alibaba Cloud SMS implementation — sends SMS via Alibaba Cloud dysmsapi SDK
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "sms", name = "provider", havingValue = "aliyun", matchIfMissing = true)
public class AliyunSmsProvider implements SmsProvider {

    private final SmsConfig smsConfig;

    private volatile AsyncClient client;

    /**
     * 获取或初始化阿里云短信客户端（懒加载）/ Get or initialize Alibaba Cloud SMS client (lazy loading)
     */
    private AsyncClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    SmsConfig.AliyunConfig aliyun = smsConfig.getAliyun();
                    StaticCredentialProvider provider = StaticCredentialProvider.create(
                            Credential.builder()
                                    .accessKeyId(aliyun.getAccessKeyId())
                                    .accessKeySecret(aliyun.getAccessKeySecret())
                                    .build()
                    );
                    client = AsyncClient.builder()
                            .credentialsProvider(provider)
                            .overrideConfiguration(ClientOverrideConfiguration.create()
                                    .setEndpointOverride("dysmsapi.aliyuncs.com"))
                            .build();
                    log.info("阿里云短信客户端初始化完成 / Alibaba Cloud SMS client initialized");
                }
            }
        }
        return client;
    }

    @Override
    public SmsSendResult send(String phoneNumber, String templateCode, String signName, Map<String, String> params) {
        try {
            SendSmsRequest request = SendSmsRequest.builder()
                    .phoneNumbers(phoneNumber)
                    .signName(signName)
                    .templateCode(templateCode)
                    .templateParam(buildTemplateParam(params))
                    .build();

            CompletableFuture<SendSmsResponse> future = getClient().sendSms(request);
            SendSmsResponse response = future.get();

            if ("OK".equals(response.getBody().getCode())) {
                log.info("阿里云短信发送成功: phone={}, templateCode={}, bizId={}",
                        phoneNumber, templateCode, response.getBody().getBizId());
                return SmsSendResult.success(phoneNumber, response.getBody().getRequestId(), response.getBody().getBizId());
            } else {
                log.warn("阿里云短信发送失败: phone={}, templateCode={}, code={}, message={}",
                        phoneNumber, templateCode, response.getBody().getCode(), response.getBody().getMessage());
                return SmsSendResult.failure(phoneNumber, response.getBody().getCode(), response.getBody().getMessage());
            }
        } catch (Exception e) {
            log.error("阿里云短信发送异常: phone={}, templateCode={}", phoneNumber, templateCode, e);
            return SmsSendResult.failure(phoneNumber, "CLIENT_ERROR", e.getMessage());
        }
    }

    /**
     * 将参数 Map 转换为阿里云模板参数 JSON 字符串 / Convert parameter Map to Alibaba Cloud template parameter JSON string
     */
    private String buildTemplateParam(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue().replace("\"", "\\\"")).append("\"");
            i++;
        }
        sb.append("}");
        return sb.toString();
    }
}
