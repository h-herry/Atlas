package com.atlas.contract.econtract.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 签署记录状态枚举 — 记录级别（不同于 SignStatus 的流程级别） /
 * Sign record status enum — record-level (different from SignStatus which is flow-level)
 *
 * <p>状态流转: PENDING → SIGNED / REJECTED / State transitions: PENDING → SIGNED / REJECTED</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Getter
@AllArgsConstructor
public enum SignRecordStatus {

    /** 待签署 / Pending signing */
    PENDING("PENDING", "待签署"),

    /** 已签署 / Signed */
    SIGNED("SIGNED", "已签署"),

    /** 已拒绝 / Rejected */
    REJECTED("REJECTED", "已拒绝");

    private final String code;
    private final String desc;

    /**
     * 根据 code 获取枚举 / Get enum by code
     */
    public static SignRecordStatus of(String code) {
        for (SignRecordStatus s : values()) {
            if (s.code.equals(code)) return s;
        }
        throw new IllegalArgumentException("未知签署记录状态: " + code);
    }
}
