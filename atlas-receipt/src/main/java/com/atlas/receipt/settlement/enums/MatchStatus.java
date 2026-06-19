package com.atlas.receipt.settlement.enums;

import lombok.Getter;

/**
 * 三单匹配状态枚举 / Three-way match status enum
 *
 * @author Atlas Team
 * @since 1.2.502
 */
@Getter
public enum MatchStatus {

    /** 匹配一致 / Matched */
    MATCHED("MATCHED", "匹配一致"),

    /** 不匹配（存在差异） / Mismatched (differences found) */
    MISMATCH("MISMATCH", "不匹配"),

    /** 待匹配 / Pending match */
    PENDING("PENDING", "待匹配");

    private final String code;
    private final String desc;

    MatchStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
