package com.atlas.contract.state;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * 合同状态枚举 — 9个状态 + 合法转换规则 /
 * Contract status enum — 9 states + legal transition rules
 */
@Getter
@AllArgsConstructor
public enum ContractStatusEnum {

    DRAFT(0, "草稿", Set.of(1)),                     // → 已提交 / → Submitted
    SUBMITTED(1, "已提交", Set.of(2, 4)),            // → 审核中 / 驳回 / → Reviewing / Rejected
    REVIEWING(2, "审核中", Set.of(3, 4)),            // → 审核通过 / 驳回 / → Approved / Rejected
    APPROVED(3, "审核通过", Set.of(5)),              // → 已签署 / → Signed
    REJECTED(4, "驳回", Set.of(1)),                  // → 已提交(重新提交) / → Submitted (resubmit)
    SIGNED(5, "已签署", Set.of(6)),                  // → 执行中 / → Executing
    EXECUTING(6, "执行中", Set.of(7, 8, 9)),         // → 变更中 / 已终止 / 已完成 / → Changing / Terminated / Completed
    CHANGING(7, "变更中", Set.of(6)),                // → 执行中(变更完成) / → Executing (change done)
    TERMINATED(8, "已终止", Set.of()),               // 终态 / Terminal state
    COMPLETED(9, "已完成", Set.of());                // 终态 / Terminal state

    private final int code;
    private final String desc;
    private final Set<Integer> allowTargets;          // 允许跳转到的状态码集合 / Set of allowed target state codes

    /** 判断能否从当前状态跳转到目标状态 / Check if transition to target state is allowed */
    public boolean canTransitionTo(int targetCode) {
        return allowTargets.contains(targetCode);
    }

    /** 根据 code 获取枚举 / Get enum by code */
    public static ContractStatusEnum of(int code) {
        for (ContractStatusEnum s : values()) {
            if (s.code == code) return s;
        }
        throw new IllegalArgumentException("未知合同状态: " + code);
    }

    /** 校验状态流转是否合法 / Validate whether state transition is legal */
    public static void validateTransition(int fromCode, int toCode) {
        ContractStatusEnum from = of(fromCode);
        if (!from.canTransitionTo(toCode)) {
            throw new IllegalStateException(
                String.format("合同状态不能从 %s(%d) 变更为 %s(%d)",
                    from.desc, fromCode, of(toCode).desc, toCode));
        }
    }
}
