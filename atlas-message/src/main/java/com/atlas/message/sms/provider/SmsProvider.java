package com.atlas.message.sms.provider;

import com.atlas.message.sms.dto.SmsSendResult;

import java.util.Map;

/**
 * 短信发送接口 — 定义统一的短信发送契约，支持多厂商切换 /
 * SMS provider interface — defines unified SMS sending contract, supports multi-vendor switching
 *
 * @author Atlas Team
 * @since 1.2.10
 */
public interface SmsProvider {

    /**
     * 发送单条短信 / Send single SMS
     *
     * @param phoneNumber  目标手机号 / Target phone number
     * @param templateCode 短信模板编码 / SMS template code
     * @param signName     短信签名 / SMS sign name
     * @param params       模板参数 / Template parameters
     * @return 发送结果 / Send result
     */
    SmsSendResult send(String phoneNumber, String templateCode, String signName, Map<String, String> params);
}
