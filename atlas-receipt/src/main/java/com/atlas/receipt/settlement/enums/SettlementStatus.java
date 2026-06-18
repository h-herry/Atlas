package com.atlas.receipt.settlement.enums;

import lombok.Getter;

/**
 * 结算单状态枚举 / Settlement status enum
 *
 * @author Atlas Team
 * @since 1.2.401
 */
@Getter
public enum SettlementStatus {

    /** 待审批 / Pending approval */
    PENDING("PENDING", "待审批"),

    /** 已审批 / Approved */
    APPROVED("APPROVED", "已审批"),

    /** 已对账 / Reconciled */
    RECONCILED("RECONCILED", "已对账"),

    /** 已付款 / Paid */
    PAID("PAID", "已付款");

    private final String code;
    private final String desc;

    SettlementStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
