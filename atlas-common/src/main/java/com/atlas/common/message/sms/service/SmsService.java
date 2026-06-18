package com.atlas.common.message.sms.service;

import com.atlas.common.message.sms.config.SmsConfig;
import com.atlas.common.message.sms.dto.SmsSendRequest;
import com.atlas.common.message.sms.dto.SmsSendResult;
import com.atlas.common.message.sms.provider.SmsProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 短信发送编排 Service — 封装短信发送逻辑，支持单条和批量发送 /
 * SMS sending orchestration Service — encapsulates SMS sending logic, supports single and batch sending
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "sms", name = "enabled", havingValue = "true")
public class SmsService {

    private final SmsProvider smsProvider;
    private final SmsConfig smsConfig;

    /**
     * 发送单条短信 / Send single SMS
     *
     * @param phoneNumber  目标手机号 / Target phone number
     * @param templateCode 短信模板编码 / SMS template code
     * @param params       模板参数 / Template parameters
     * @return 发送结果 / Send result
     */
    public SmsSendResult sendSms(String phoneNumber, String templateCode, Map<String, String> params) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            log.warn("短信发送跳过，手机号为空 / SMS send skipped, phone number is empty");
            return SmsSendResult.failure("", "EMPTY_PHONE", "手机号为空 / Phone number is empty");
        }

        String signName = getSignName();
        SmsSendResult result = smsProvider.send(phoneNumber, templateCode, signName, params);
        log.info("短信发送完成: phone={}, templateCode={}, success={}",
                maskPhone(phoneNumber), templateCode, result.isSuccess());
        return result;
    }

    /**
     * 批量发送短信（异步）/ Batch send SMS (async)
     *
     * @param phoneNumbers 手机号列表 / Phone number list
     * @param templateCode 短信模板编码 / SMS template code
     * @param params       模板参数 / Template parameters
     */
    @Async
    public void sendBatchSms(List<String> phoneNumbers, String templateCode, Map<String, String> params) {
        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            log.warn("批量短信发送跳过，手机号列表为空 / Batch SMS skipped, phone number list is empty");
            return;
        }

        List<SmsSendResult> results = new ArrayList<>();
        for (String phone : phoneNumbers) {
            SmsSendResult result = sendSms(phone, templateCode, params);
            results.add(result);
        }

        long successCount = results.stream().filter(SmsSendResult::isSuccess).count();
        long failCount = results.size() - successCount;
        log.info("批量短信发送完成: total={}, success={}, fail={}", phoneNumbers.size(), successCount, failCount);
    }

    /**
     * 获取短信签名 / Get SMS sign name
     */
    private String getSignName() {
        return smsConfig.getAliyun().getSignName();
    }

    /**
     * 手机号脱敏：保留前3后4，中间用 **** 替换 / Mask phone number: keep first 3 and last 4 digits
     */
    private String maskPhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() <= 7) {
            return "***";
        }
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }
}
