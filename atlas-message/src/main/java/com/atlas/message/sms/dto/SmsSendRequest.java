package com.atlas.message.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 短信发送请求 DTO — 封装短信发送所需全部参数 /
 * SMS send request DTO — encapsulates all parameters needed for SMS sending
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsSendRequest {

    /** 目标手机号（单个）/ Target phone number (single) */
    private String phoneNumber;

    /** 目标手机号列表（批量）/ Target phone number list (batch) */
    private List<String> phoneNumbers;

    /** 短信模板编码（阿里云模板CODE / 腾讯云模板ID）/ SMS template code (Aliyun template CODE / Tencent template ID) */
    private String templateCode;

    /** 模板参数（key → value）/ Template parameters (key → value) */
    private Map<String, String> params;
}
