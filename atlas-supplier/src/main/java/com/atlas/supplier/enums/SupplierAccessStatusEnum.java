package com.atlas.supplier.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * 供应商准入状态枚举 — 6个状态 + 合法转换规则 /
 * Supplier access status enum — 6 states + legal transition rules
 *
 * <p>状态流程 / State flow：PENDING → INITIAL_PASSED → FIELD_PASSED → FINAL_PASSED → ONBOARDED
 * <br>任意状态均可跳转到 REJECTED（驳回） / Any state can transition to REJECTED.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Getter
@AllArgsConstructor
public enum SupplierAccessStatusEnum {

    PENDING(0, "待审核 / Pending", Set.of(1, 4)),                 // → 初审通过 / 驳回 → Initial pass / Rejected
    INITIAL_PASSED(1, "初审通过 / Initial Passed", Set.of(2, 4)),        // → 现场考察通过 / 驳回 → Field pass / Rejected
    FIELD_PASSED(2, "现场考察通过 / Field Passed", Set.of(3, 4)),      // → 终审通过 / 驳回 → Final pass / Rejected
    FINAL_PASSED(3, "终审通过 / Final Passed", Set.of(5)),             // → 已入库 → Onboarded
    REJECTED(4, "驳回 / Rejected", Set.of()),                     // 终态 / Terminal state
    ONBOARDED(5, "已入库 / Onboarded", Set.of());                  // 终态 / Terminal state

    private final int code;
    private final String description;
    private final Set<Integer> allowTargets;

    /** 判断是否可以转换到目标状态 / Check if can transition to target state */
    public boolean canTransitionTo(int targetCode) {
        return allowTargets.contains(targetCode);
    }

    /** 根据 code 获取枚举值 / Get enum by code */
    public static SupplierAccessStatusEnum of(int code) {
        for (SupplierAccessStatusEnum s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知供应商准入状态 / Unknown access status: " + code);
    }

    /** 校验状态转换合法性 / Validate state transition legality */
    public static void validateTransition(int fromCode, int toCode) {
        SupplierAccessStatusEnum from = of(fromCode);
        if (!from.canTransitionTo(toCode)) {
            throw new IllegalStateException(
                String.format("供应商准入状态不能从 %s(%d) 变更为 %s(%d) / Cannot transition from %s(%d) to %s(%d)",
                    from.description, fromCode, of(toCode).description, toCode,
                    from.description, fromCode, of(toCode).description, toCode));
        }
    }
}
