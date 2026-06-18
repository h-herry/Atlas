package com.atlas.purchase.inquiry.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报价状态枚举 / Quote status enum
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Getter
@AllArgsConstructor
public enum QuoteStatus {

    /** 已提交 / Submitted */
    SUBMITTED("SUBMITTED", "已提交"),

    /** 已撤回 / Withdrawn */
    WITHDRAWN("WITHDRAWN", "已撤回"),

    /** 已接受 / Accepted */
    ACCEPTED("ACCEPTED", "已接受"),

    /** 已拒绝 / Rejected */
    REJECTED("REJECTED", "已拒绝");

    private final String code;
    private final String label;

    public static QuoteStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (QuoteStatus e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        return null;
    }
}
