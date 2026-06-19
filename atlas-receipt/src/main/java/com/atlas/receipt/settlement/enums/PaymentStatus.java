package com.atlas.receipt.settlement.enums;

import lombok.Getter;

/**
 * 付款状态枚举 / Payment status enum
 *
 * @author Atlas Team
 * @since 1.2.502
 */
@Getter
public enum PaymentStatus {

    /** 待审批 / Pending approval */
    PENDING("PENDING", "待审批"),

    /** 已审批 / Approved */
    APPROVED("APPROVED", "已审批"),

    /** 已支付 / Paid */
    PAID("PAID", "已支付"),

    /** 已取消 / Cancelled */
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String desc;

    PaymentStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
