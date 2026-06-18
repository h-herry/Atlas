package com.atlas.contract.econtract.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 签署类型枚举 / Sign type enum
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Getter
@AllArgsConstructor
public enum SignType {

    /** 在线签署 / Online signing */
    ONLINE("ONLINE", "在线签署"),

    /** 线下签署 / Offline signing */
    OFFLINE("OFFLINE", "线下签署");

    private final String code;
    private final String desc;
}
