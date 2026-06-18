package com.atlas.common.message.mail.template;

import lombok.Getter;

/**
 * 预置邮件模板枚举 — 定义系统内所有邮件模板的编码、标题和适用场景 /
 * Predefined mail template enum — defines all mail template codes, titles and applicable scenarios
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Getter
public enum MailTemplateEnum {

    /** 入驻申请提交成功 / Supplier registration submitted */
    SUPPLIER_REGISTER_SUCCESS(
            "SUPPLIER_REGISTER_SUCCESS",
            "入驻申请提交成功 / Registration Submitted",
            "供应商自助注册后推送 / Push after supplier self-registration"
    ),

    /** 入驻审核通过 / Supplier registration approved */
    SUPPLIER_APPROVED(
            "SUPPLIER_APPROVED",
            "入驻审核通过通知 / Registration Approved",
            "审批通过后推送 / Push after approval passed"
    ),

    /** 入驻审核未通过 / Supplier registration rejected */
    SUPPLIER_REJECTED(
            "SUPPLIER_REJECTED",
            "入驻审核未通过通知 / Registration Rejected",
            "审批驳回后推送 / Push after approval rejected"
    ),

    /** 新采购订单通知 / New purchase order notification */
    ORDER_CREATED(
            "ORDER_CREATED",
            "新采购订单通知 / New Purchase Order",
            "采购员下达订单后推送 / Push after buyer places order"
    ),

    /** 订单确认通知 / Order confirmation notification */
    ORDER_CONFIRMED(
            "ORDER_CONFIRMED",
            "订单确认通知 / Order Confirmed",
            "供应商确认接单后推送 / Push after supplier confirms order"
    ),

    /** 发货通知 / Shipment notification */
    DELIVERY_SHIPPED(
            "DELIVERY_SHIPPED",
            "发货通知 / Shipment Notification",
            "供应商发货后推送 / Push after supplier ships goods"
    ),

    /** 交期延迟预警 / Delivery delay alert */
    DELIVERY_DELAYED(
            "DELIVERY_DELAYED",
            "交期延迟预警 / Delivery Delay Alert",
            "延迟报备后推送 / Push after delay reported"
    ),

    /** 合同待签署通知 / Contract signing notification */
    CONTRACT_SIGNING(
            "CONTRACT_SIGNING",
            "合同待签署通知 / Contract Pending Signature",
            "发起合同签署后推送 / Push after contract signing initiated"
    ),

    /** 竞价邀请通知 / Bidding invitation notification */
    BIDDING_INVITATION(
            "BIDDING_INVITATION",
            "竞价邀请通知 / Bidding Invitation",
            "邀请供应商参与竞价后推送 / Push after inviting supplier to bid"
    ),

    /** 竞价结果通知 / Bidding result notification */
    BIDDING_RESULT(
            "BIDDING_RESULT",
            "竞价结果通知 / Bidding Result",
            "竞价结束后推送 / Push after bidding ends"
    ),

    /** 质检不合格通知 / Quality inspection failure notification */
    QUALITY_INSPECTION_FAIL(
            "QUALITY_INSPECTION_FAIL",
            "质检不合格通知 / Quality Inspection Failed",
            "IQC不合格后推送 / Push after IQC failure"
    ),

    /** 对账单通知 / Settlement statement notification */
    SETTLEMENT_STATEMENT(
            "SETTLEMENT_STATEMENT",
            "对账单通知 / Settlement Statement",
            "生成对账单后推送 / Push after statement generated"
    );

    /** 模板编码 / Template code */
    private final String code;

    /** 邮件标题 / Mail title */
    private final String title;

    /** 适用场景 / Applicable scenario */
    private final String scenario;

    MailTemplateEnum(String code, String title, String scenario) {
        this.code = code;
        this.title = title;
        this.scenario = scenario;
    }

    /**
     * 根据模板编码查找枚举 / Find enum by template code
     *
     * @param code 模板编码 / Template code
     * @return 对应枚举，未找到返回 null / Corresponding enum, null if not found
     */
    public static MailTemplateEnum fromCode(String code) {
        for (MailTemplateEnum template : values()) {
            if (template.code.equals(code)) {
                return template;
            }
        }
        return null;
    }
}
