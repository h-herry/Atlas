package com.atlas.contract.econtract.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 合同模板分类枚举 / Contract template category enum
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Getter
@AllArgsConstructor
public enum TemplateCategory {

    /** 采购合同 / Purchase contract */
    PURCHASE("PURCHASE", "采购合同"),

    /** 服务合同 / Service contract */
    SERVICE("SERVICE", "服务合同"),

    /** 保密协议 / Non-disclosure agreement */
    NDA("NDA", "保密协议"),

    /** 框架协议 / Framework agreement */
    FRAMEWORK("FRAMEWORK", "框架协议");

    private final String code;
    private final String desc;
}
