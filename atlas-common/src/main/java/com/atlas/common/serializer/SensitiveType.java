package com.atlas.common.serializer;

/**
 * 敏感数据脱敏类型枚举 / Sensitive data masking type enum
 *
 * @author Atlas Team
 * @since 2.0.0
 */
public enum SensitiveType {

    /** 银行账号 / Bank account: 保留前4后4，中间 * 替换 / keep first 4 + last 4, middle masked */
    BANK_CARD,

    /** 手机号 / Phone number: 保留前3后4，中间 * 替换 / keep first 3 + last 4, middle masked */
    PHONE,

    /** 身份证号 / ID card: 保留前1后1，中间 * 替换 / keep first 1 + last 1, middle masked */
    ID_CARD,

    /** 邮箱 / Email: 保留首字符和域名，中间 * 替换 / keep first char + domain, middle masked */
    EMAIL,

    /** 详细地址 / Address: 只保留省市部分，其余 * 替换 / keep province + city only, rest masked */
    ADDRESS
}
