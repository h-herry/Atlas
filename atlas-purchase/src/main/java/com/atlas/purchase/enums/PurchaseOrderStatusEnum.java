package com.atlas.purchase.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * 采购订单状态枚举 — 6个状态 + 合法转换规则 /
 * Purchase order status enum — 6 states + valid transition rules
 *
 * <p>状态流程：DRAFT → SUBMITTED → APPROVED → EXECUTING → COMPLETED /
 * State flow: DRAFT → SUBMITTED → APPROVED → EXECUTING → COMPLETED
 * <br>任意状态均可跳转到 CANCELLED。 / Any state can jump to CANCELLED.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Getter
@AllArgsConstructor
public enum PurchaseOrderStatusEnum {

    DRAFT(0, "草稿", Set.of(1, 5)),                    // → 已提交 / 已取消 / → submitted / cancelled
    SUBMITTED(1, "已提交", Set.of(2, 5)),               // → 已审批 / 已取消 / → approved / cancelled
    APPROVED(2, "已审批", Set.of(3, 5)),               // → 执行中 / 已取消 / → executing / cancelled
    EXECUTING(3, "执行中", Set.of(4, 5)),              // → 已完成 / 已取消 / → completed / cancelled
    COMPLETED(4, "已完成", Set.of()),                  // 终态 / Terminal
    CANCELLED(5, "已取消", Set.of());                  // 终态 / Terminal

    private final int code;
    private final String description;
    private final Set<Integer> allowTargets;

    public boolean canTransitionTo(int targetCode) {
        return allowTargets.contains(targetCode);
    }

    public static PurchaseOrderStatusEnum of(int code) {
        for (PurchaseOrderStatusEnum s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知采购订单状态: " + code);
    }

    public static void validateTransition(int fromCode, int toCode) {
        PurchaseOrderStatusEnum from = of(fromCode);
        if (!from.canTransitionTo(toCode)) {
            throw new IllegalStateException(
                String.format("采购订单状态不能从 %s(%d) 变更为 %s(%d)",
                    from.description, fromCode, of(toCode).description, toCode));
        }
    }
}
