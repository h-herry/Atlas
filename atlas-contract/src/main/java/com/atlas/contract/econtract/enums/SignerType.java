package com.atlas.contract.econtract.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 签署人类型枚举 / Signer type enum
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Getter
@AllArgsConstructor
public enum SignerType {

    /** 内部签署人 / Internal signer */
    INTERNAL("INTERNAL", "内部签署人"),

    /** 外部签署人 / External signer */
    EXTERNAL("EXTERNAL", "外部签署人");

    private final String code;
    private final String desc;
}
