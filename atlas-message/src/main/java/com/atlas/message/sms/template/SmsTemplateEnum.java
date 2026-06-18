package com.atlas.message.sms.template;

import lombok.Getter;

/**
 * 短信模板枚举 — 与阿里云短信模板CODE一一对应 /
 * SMS template enum — one-to-one mapping with Alibaba Cloud SMS template CODEs
 *
 * <p>所有模板内容需提前在阿里云短信控制台审核通过 /
 * All template content must be pre-approved in Alibaba Cloud SMS console</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Getter
public enum SmsTemplateEnum {

    /** 入驻申请提交 / Supplier registration submitted */
    SMS_SUPPLIER_REG(
            "SMS_SUPPLIER_REG",
            "SMS_123456001",
            "【SRM】您的入驻申请已提交，我们将尽快审核，请留意通知。申请编号：${applyId}"
    ),

    /** 入驻审核通过 / Supplier registration approved */
    SMS_SUPPLIER_APPROVED(
            "SMS_SUPPLIER_APPROVED",
            "SMS_123456002",
            "【SRM】恭喜！您的入驻申请已通过审核，请登录系统完善企业信息。"
    ),

    /** 新订单通知 / New order notification */
    SMS_ORDER_CREATED(
            "SMS_ORDER_CREATED",
            "SMS_123456003",
            "【SRM】您收到新的采购订单 ${orderNo}，请登录系统确认。"
    ),

    /** 发货通知 / Shipment notification */
    SMS_ORDER_DELIVERY(
            "SMS_ORDER_DELIVERY",
            "SMS_123456004",
            "【SRM】订单 ${orderNo} 已发货，物流单号：${trackingNo}，物流公司：${carrier}"
    ),

    /** 竞价邀请 / Bidding invitation */
    SMS_BIDDING_INVITE(
            "SMS_BIDDING_INVITE",
            "SMS_123456005",
            "【SRM】您被邀请参与询价 ${inquiryNo} 的竞价，截止时间：${deadline}，请登录系统报价。"
    ),

    /** 竞价结果 / Bidding result */
    SMS_BIDDING_RESULT(
            "SMS_BIDDING_RESULT",
            "SMS_123456006",
            "【SRM】询价 ${inquiryNo} 竞价已结束，排名第 ${rank} 名。请登录查看结果。"
    ),

    /** 交期变更 / Delivery date change */
    SMS_DELIVERY_DELAY(
            "SMS_DELIVERY_DELAY",
            "SMS_123456007",
            "【SRM】订单 ${orderNo} 交期变更，新预计交期：${newDate}。请登录查看详情。"
    ),

    /** 合同待签署 / Contract pending signature */
    SMS_CONTRACT_SIGN(
            "SMS_CONTRACT_SIGN",
            "SMS_123456008",
            "【SRM】合同 ${contractNo} 待您签署，请登录系统处理。"
    );

    /** 系统内部编码 / Internal system code */
    private final String code;

    /** 阿里云模板CODE / Alibaba Cloud template CODE */
    private final String aliyunTemplateCode;

    /** 模板内容示例（实际以阿里云审核通过为准）/ Template content sample (actual version subject to Alibaba Cloud approval) */
    private final String contentSample;

    SmsTemplateEnum(String code, String aliyunTemplateCode, String contentSample) {
        this.code = code;
        this.aliyunTemplateCode = aliyunTemplateCode;
        this.contentSample = contentSample;
    }

    /**
     * 根据系统内部编码查找枚举 / Find enum by internal system code
     *
     * @param code 系统内部编码 / Internal system code
     * @return 对应枚举，未找到返回 null / Corresponding enum, null if not found
     */
    public static SmsTemplateEnum fromCode(String code) {
        for (SmsTemplateEnum template : values()) {
            if (template.code.equals(code)) {
                return template;
            }
        }
        return null;
    }

    /**
     * 根据阿里云模板CODE查找枚举 / Find enum by Alibaba Cloud template CODE
     *
     * @param aliyunCode 阿里云模板CODE / Alibaba Cloud template CODE
     * @return 对应枚举，未找到返回 null / Corresponding enum, null if not found
     */
    public static SmsTemplateEnum fromAliyunCode(String aliyunCode) {
        for (SmsTemplateEnum template : values()) {
            if (template.aliyunTemplateCode.equals(aliyunCode)) {
                return template;
            }
        }
        return null;
    }
}
