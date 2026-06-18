package com.atlas.contract.econtract.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 履约状态枚举 / Performance status enum
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Getter
@AllArgsConstructor
public enum PerformanceStatus {

    /** 未开始 / Not started */
    NOT_STARTED("NOT_STARTED", "未开始"),

    /** 进行中 / In progress */
    IN_PROGRESS("IN_PROGRESS", "进行中"),

    /** 已完成 / Completed */
    COMPLETED("COMPLETED", "已完成"),

    /** 已违约 / Breached */
    BREACHED("BREACHED", "已违约");

    private final String code;
    private final String desc;

    /**
     * 根据 code 获取枚举 / Get enum by code
     */
    public static PerformanceStatus of(String code) {
        for (PerformanceStatus s : values()) {
            if (s.code.equals(code)) return s;
        }
        throw new IllegalArgumentException("未知履约状态: " + code);
    }
}
