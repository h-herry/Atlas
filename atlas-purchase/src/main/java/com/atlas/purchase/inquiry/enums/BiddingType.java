package com.atlas.purchase.inquiry.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 竞价模式枚举 / Bidding type enum
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Getter
@AllArgsConstructor
public enum BiddingType {

    /** 公开竞价 — 报价和排名实时可见 / Open bidding — bids and rankings visible in real-time */
    OPEN("OPEN", "公开竞价"),

    /** 密封竞价 — 报价不公开，结束后揭晓 / Sealed bidding — bids hidden until auction ends */
    SEALED("SEALED", "密封竞价");

    private final String code;
    private final String label;

    public static BiddingType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (BiddingType e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        return null;
    }
}
