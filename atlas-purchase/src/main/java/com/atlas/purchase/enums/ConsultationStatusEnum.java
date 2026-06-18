package com.atlas.purchase.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * 竞争性磋商状态枚举 — 8个状态 + 合法转换规则 /
 * Competitive consultation status enum — 8 states + valid transition rules
 *
 * <p>状态流程：DRAFT → ANNOUNCEMENT → RESPONSE → CONSULTING → FINAL_OFFER → COMPREHENSIVE_REVIEW → AWARDED /
 * State flow: DRAFT → ANNOUNCEMENT → RESPONSE → CONSULTING → FINAL_OFFER → COMPREHENSIVE_REVIEW → AWARDED
 * <br>任意状态（除已定标）均可跳转到 TERMINATED。 / Any state (except AWARDED) can jump to TERMINATED.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Getter
@AllArgsConstructor
public enum ConsultationStatusEnum {

    DRAFT(0, "草稿", Set.of(1, 7)),                         // → 公告期 / 已终止 / → announcement / terminated
    ANNOUNCEMENT(1, "公告期", Set.of(2, 7)),                // → 响应收集 / 已终止 / → response / terminated
    RESPONSE(2, "响应收集", Set.of(3, 7)),                  // → 磋商中 / 已终止 / → consulting / terminated
    CONSULTING(3, "磋商中", Set.of(4, 7)),                  // → 最终报价 / 已终止 / → final offer / terminated
    FINAL_OFFER(4, "最终报价", Set.of(5, 7)),               // → 综合评审 / 已终止 / → comprehensive review / terminated
    COMPREHENSIVE_REVIEW(5, "综合评审", Set.of(6, 7)),      // → 已定标 / 已终止 / → awarded / terminated
    AWARDED(6, "已定标", Set.of()),                         // 终态 / Terminal
    TERMINATED(7, "已终止", Set.of());                      // 终态 / Terminal

    private final int code;
    private final String description;
    private final Set<Integer> allowTargets;

    public boolean canTransitionTo(int targetCode) {
        return allowTargets.contains(targetCode);
    }

    public static ConsultationStatusEnum of(int code) {
        for (ConsultationStatusEnum s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知磋商状态: " + code);
    }

    public static void validateTransition(int fromCode, int toCode) {
        ConsultationStatusEnum from = of(fromCode);
        if (!from.canTransitionTo(toCode)) {
            throw new IllegalStateException(
                String.format("磋商状态不能从 %s(%d) 变更为 %s(%d)",
                    from.description, fromCode, of(toCode).description, toCode));
        }
    }
}
