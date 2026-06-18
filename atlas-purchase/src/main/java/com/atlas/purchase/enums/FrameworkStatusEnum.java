package com.atlas.purchase.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * 框架协议状态枚举 — 8个状态 + 合法转换规则 /
 * Framework status enum — 8 states + valid transition rules
 *
 * <p>状态流程：DRAFT → COLLECTING → REVIEWING → SHORTLISTED → ACTIVE → EXECUTING /
 * State flow: DRAFT → COLLECTING → REVIEWING → SHORTLISTED → ACTIVE → EXECUTING
 * <br>任意非终态均可跳转到 EXPIRED / TERMINATED。 / Any non-terminal state can jump to EXPIRED / TERMINATED.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Getter
@AllArgsConstructor
public enum FrameworkStatusEnum {

    DRAFT(0, "草稿", Set.of(1, 6, 7)),                      // → 征集中 / 已过期 / 已终止 / → collecting / expired / terminated
    COLLECTING(1, "入围征集中", Set.of(2, 6, 7)),            // → 评审中 / 已过期 / 已终止 / → reviewing / expired / terminated
    REVIEWING(2, "评审中", Set.of(3, 6, 7)),                 // → 已入围 / 已过期 / 已终止 / → shortlisted / expired / terminated
    SHORTLISTED(3, "已入围", Set.of(4, 6, 7)),               // → 已生效 / 已过期 / 已终止 / → active / expired / terminated
    ACTIVE(4, "已生效", Set.of(5, 6, 7)),                    // → 执行中 / 已过期 / 已终止 / → executing / expired / terminated
    EXECUTING(5, "执行中", Set.of(6, 7)),                    // → 已过期 / 已终止 / → expired / terminated
    EXPIRED(6, "已过期", Set.of()),                          // 终态 / Terminal
    TERMINATED(7, "已终止", Set.of());                       // 终态 / Terminal

    private final int code;
    private final String description;
    private final Set<Integer> allowTargets;

    /**
     * 判断是否可以转换到目标状态 / Check if transition to target state is allowed
     */
    public boolean canTransitionTo(int targetCode) {
        return allowTargets.contains(targetCode);
    }

    /**
     * 根据 code 获取枚举 / Get enum by code
     */
    public static FrameworkStatusEnum of(int code) {
        for (FrameworkStatusEnum s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知框架协议状态: " + code);
    }

    /**
     * 校验状态转换是否合法 / Validate state transition legality
     */
    public static void validateTransition(int fromCode, int toCode) {
        FrameworkStatusEnum from = of(fromCode);
        if (!from.canTransitionTo(toCode)) {
            throw new IllegalStateException(
                String.format("框架协议状态不能从 %s(%d) 变更为 %s(%d)",
                    from.description, fromCode, of(toCode).description, toCode));
        }
    }
}
