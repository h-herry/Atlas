package com.atlas.message.sms.provider;

import com.atlas.message.sms.config.SmsConfig;
import com.atlas.message.sms.dto.SmsSendResult;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 腾讯云短信实现 — 基于腾讯云 SMS SDK 发送短信（预留） /
 * Tencent Cloud SMS implementation — sends SMS via Tencent Cloud SMS SDK (reserved)
 *
 * <p>当前为预留实现，需配置 sms.provider=tencent 后启用 /
 * Currently reserved implementation, enable by configuring sms.provider=tencent</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "sms", name = "provider", havingValue = "tencent")
public class TencentSmsProvider implements SmsProvider {

    private final SmsConfig smsConfig;

    private volatile SmsClient client;

    /**
     * 获取或初始化腾讯云短信客户端（懒加载）/ Get or initialize Tencent Cloud SMS client (lazy loading)
     */
    private SmsClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    SmsConfig.TencentConfig tencent = smsConfig.getTencent();
                    Credential cred = new Credential(tencent.getAppId(), tencent.getAppKey());
                    client = new SmsClient(cred, "ap-guangzhou");
                    log.info("腾讯云短信客户端初始化完成 / Tencent Cloud SMS client initialized");
                }
            }
        }
        return client;
    }

    @Override
    public SmsSendResult send(String phoneNumber, String templateCode, String signName, Map<String, String> params) {
        try {
            SmsConfig.TencentConfig tencent = smsConfig.getTencent();

            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppId(tencent.getAppId());
            req.setSignName(signName);
            req.setTemplateId(templateCode);

            // 手机号需加 +86 前缀 / Phone number needs +86 prefix
            String[] phoneNumbers = {phoneNumber.startsWith("+") ? phoneNumber : "+86" + phoneNumber};
            req.setPhoneNumberSet(phoneNumbers);

            if (params != null && !params.isEmpty()) {
                String[] templateParams = params.values().toArray(new String[0]);
                req.setTemplateParamSet(templateParams);
            }

            SendSmsResponse res = getClient().SendSms(req);

            if (res.getSendStatusSet() != null && res.getSendStatusSet().length > 0
                    && "Ok".equals(res.getSendStatusSet()[0].getCode())) {
                log.info("腾讯云短信发送成功: phone={}, templateCode={}", phoneNumber, templateCode);
                return SmsSendResult.success(phoneNumber, res.getRequestId(),
                        res.getSendStatusSet()[0].getSerialNo());
            } else {
                String code = res.getSendStatusSet() != null && res.getSendStatusSet().length > 0
                        ? res.getSendStatusSet()[0].getCode() : "UNKNOWN";
                String msg = res.getSendStatusSet() != null && res.getSendStatusSet().length > 0
                        ? res.getSendStatusSet()[0].getMessage() : "Unknown error";
                log.warn("腾讯云短信发送失败: phone={}, templateCode={}, code={}, message={}",
                        phoneNumber, templateCode, code, msg);
                return SmsSendResult.failure(phoneNumber, code, msg);
            }
        } catch (TencentCloudSDKException e) {
            log.error("腾讯云短信发送异常: phone={}, templateCode={}", phoneNumber, templateCode, e);
            return SmsSendResult.failure(phoneNumber, "SDK_ERROR", e.getMessage());
        }
    }
}
