package com.atlas.purchase.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * 竞争性谈判状态枚举 — 7个状态 + 合法转换规则 /
 * Competitive negotiation status enum — 7 states + valid transition rules
 *
 * <p>状态流程：DRAFT → PUBLISHED → NEGOTIATING → OFFER_COLLECTED → REVIEWING → AWARDED /
 * State flow: DRAFT → PUBLISHED → NEGOTIATING → OFFER_COLLECTED → REVIEWING → AWARDED
 * <br>任意状态（除已定标）均可跳转到 TERMINATED。 / Any state (except AWARDED) can jump to TERMINATED.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Getter
@AllArgsConstructor
public enum NegotiationStatusEnum {

    DRAFT(0, "草稿", Set.of(1, 6)),                       // → 已发布 / 已终止 / → published / terminated
    PUBLISHED(1, "已发布", Set.of(2, 6)),                 // → 谈判中 / 已终止 / → negotiating / terminated
    NEGOTIATING(2, "谈判中", Set.of(3, 6)),               // → 报价收集 / 已终止 / → offer collected / terminated
    OFFER_COLLECTED(3, "报价已收集", Set.of(4, 6)),       // → 评审中 / 已终止 / → reviewing / terminated
    REVIEWING(4, "评审中", Set.of(5, 6)),                 // → 已定标 / 已终止 / → awarded / terminated
    AWARDED(5, "已定标", Set.of()),                       // 终态 / Terminal
    TERMINATED(6, "已终止", Set.of());                    // 终态 / Terminal

    private final int code;
    private final String description;
    private final Set<Integer> allowTargets;

    public boolean canTransitionTo(int targetCode) {
        return allowTargets.contains(targetCode);
    }

    public static NegotiationStatusEnum of(int code) {
        for (NegotiationStatusEnum s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知谈判状态: " + code);
    }

    public static void validateTransition(int fromCode, int toCode) {
        NegotiationStatusEnum from = of(fromCode);
        if (!from.canTransitionTo(toCode)) {
            throw new IllegalStateException(
                String.format("谈判状态不能从 %s(%d) 变更为 %s(%d)",
                    from.description, fromCode, of(toCode).description, toCode));
        }
    }
}
