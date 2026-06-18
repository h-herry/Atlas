package com.atlas.contract.econtract.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * 签署状态枚举 — 状态机: DRAFT→SIGNING→COMPLETED/EXPIRED/CANCELLED /
 * Sign status enum — state machine: DRAFT→SIGNING→COMPLETED/EXPIRED/CANCELLED
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Getter
@AllArgsConstructor
public enum SignStatus {

    /** 草稿 / Draft */
    DRAFT("DRAFT", "草稿", Set.of("SIGNING", "CANCELLED")),

    /** 签署中 / Signing */
    SIGNING("SIGNING", "签署中", Set.of("COMPLETED", "EXPIRED", "CANCELLED")),

    /** 已完成 / Completed */
    COMPLETED("COMPLETED", "已完成", Set.of()),

    /** 已过期 / Expired */
    EXPIRED("EXPIRED", "已过期", Set.of()),

    /** 已取消 / Cancelled */
    CANCELLED("CANCELLED", "已取消", Set.of());

    private final String code;
    private final String desc;
    private final Set<String> allowTargets;

    /**
     * 判断能否从当前状态跳转到目标状态 / Check if transition to target status is allowed
     */
    public boolean canTransitionTo(String targetCode) {
        return allowTargets.contains(targetCode);
    }

    /**
     * 根据 code 获取枚举 / Get enum by code
     */
    public static SignStatus of(String code) {
        for (SignStatus s : values()) {
            if (s.code.equals(code)) return s;
        }
        throw new IllegalArgumentException("未知签署状态: " + code);
    }

    /**
     * 校验状态流转是否合法 / Validate whether status transition is legal
     */
    public static void validateTransition(String fromCode, String toCode) {
        SignStatus from = of(fromCode);
        if (!from.canTransitionTo(toCode)) {
            throw new IllegalStateException(
                String.format("签署状态不能从 %s(%s) 变更为 %s(%s)",
                    from.desc, fromCode, of(toCode).desc, toCode));
        }
    }
}
