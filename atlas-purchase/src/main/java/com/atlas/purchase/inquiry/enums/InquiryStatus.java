package com.atlas.purchase.inquiry.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 询价单状态枚举 / Inquiry status enum
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Getter
@AllArgsConstructor
public enum InquiryStatus {

    /** 草稿 — 未发布，可编辑 / Draft — not published, editable */
    DRAFT("DRAFT", "草稿"),

    /** 已发布 — 供应商可见，接受报价中 / Published — visible to suppliers, accepting quotes */
    PUBLISHED("PUBLISHED", "已发布"),

    /** 报价中 — 报价窗口开启 / Quoting — quote window open */
    QUOTING("QUOTING", "报价中"),

    /** 已比价 — 报价截止，对比中 / Compared — quote deadline reached, under comparison */
    COMPARED("COMPARED", "已比价"),

    /** 已关闭 — 询价结束 / Closed — inquiry ended */
    CLOSED("CLOSED", "已关闭");

    private final String code;
    private final String label;

    public static InquiryStatus fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (InquiryStatus e : values()) {
            if (e.code.equalsIgnoreCase(code)) {
                return e;
            }
        }
        return null;
    }
}
