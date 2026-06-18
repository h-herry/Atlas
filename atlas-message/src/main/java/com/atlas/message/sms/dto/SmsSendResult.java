package com.atlas.message.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短信发送结果 DTO — 单条短信发送的回执 /
 * SMS send result DTO — receipt for single SMS send
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsSendResult {

    /** 是否发送成功 / Whether send succeeded */
    private boolean success;

    /** 目标手机号 / Target phone number */
    private String phoneNumber;

    /** 请求ID（云厂商返回）/ Request ID (returned by cloud vendor) */
    private String requestId;

    /** 发送流水号 / Send serial number */
    private String bizId;

    /** 错误码（失败时）/ Error code (when failed) */
    private String errorCode;

    /** 错误信息（失败时）/ Error message (when failed) */
    private String errorMessage;

    /**
     * 构建成功结果 / Build success result
     */
    public static SmsSendResult success(String phoneNumber, String requestId, String bizId) {
        return SmsSendResult.builder()
                .success(true)
                .phoneNumber(phoneNumber)
                .requestId(requestId)
                .bizId(bizId)
                .build();
    }

    /**
     * 构建失败结果 / Build failure result
     */
    public static SmsSendResult failure(String phoneNumber, String errorCode, String errorMessage) {
        return SmsSendResult.builder()
                .success(false)
                .phoneNumber(phoneNumber)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build();
    }
}
