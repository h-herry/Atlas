package com.atlas.purchase.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 开标状态枚举 — 询比采购定标状态 / Award status enum — inquiry purchase award status
 *
 * <p>状态流转: PENDING → AWARDED / CANCELLED / State transitions: PENDING → AWARDED / CANCELLED</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Getter
@AllArgsConstructor
public enum AwardStatusEnum {

    /** 待定标 / Pending award */
    PENDING("PENDING", "待定标"),

    /** 已定标 / Awarded */
    AWARDED("AWARDED", "已定标"),

    /** 已取消 / Cancelled */
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;

    /**
     * 根据 code 获取枚举 / Get enum by code
     */
    public static AwardStatusEnum of(String code) {
        for (AwardStatusEnum s : values()) {
            if (s.code.equals(code)) return s;
        }
        throw new IllegalArgumentException("未知定标状态: " + code);
    }
}
