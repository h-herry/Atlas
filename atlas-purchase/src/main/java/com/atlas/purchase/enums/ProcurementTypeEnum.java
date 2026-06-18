package com.atlas.purchase.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 采购方式枚举 — 符合政府采购规范的 9 种采购模式 /
 * Procurement type enum — 9 procurement modes compliant with government procurement regulations
 *
 * <p>code 值与数据库 procurement_type 字段对齐，不可随意变更已有值的语义。 /
 * Code values align with the procurement_type database column; semantics of existing values must not be changed arbitrarily.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Getter
@AllArgsConstructor
public enum ProcurementTypeEnum {

    /** 公开招标：发布公告向全社会邀请，适用于金额大、竞争充分、需求明确的项目 / Open bidding: public announcement, suitable for large-amount, fully competitive projects with clear requirements */
    OPEN_BIDDING(1, "公开招标", "发布公告向全社会邀请，适用于金额大、竞争充分、需求明确的项目"),

    /** 邀请招标：向特定几家供应商发出邀请，适用于专业性强、供应商范围有限的场景 / Invited bidding: invite selected suppliers, suitable for specialized fields with limited supplier base */
    INVITED_BIDDING(2, "邀请招标", "向特定几家供应商发出邀请，适用于专业性强、供应商范围有限的场景"),

    /** 询比采购：向3家及以上供应商询价，一次性不可更改报价，适用于标准化程度高的货物 / Inquiry-comparison: solicit quotes from 3+ suppliers, irrevocable one-time quotes, suitable for standardized goods */
    INQUIRY(3, "询比采购", "向3家及以上供应商询价，一次性不可更改报价，适用于标准化程度高的货物"),

    /** 竞价采购：3家及以上供应商多轮次公开竞争报价，以最终报价定标，适用于大宗原材料 / Auction: 3+ suppliers compete in multi-round public bidding, awarded by final price, suitable for bulk raw materials */
    AUCTION(4, "竞价采购", "3家及以上供应商多轮次公开竞争报价，以最终报价定标，适用于大宗原材料"),

    /** 竞争性谈判：与2家及以上供应商分别多轮谈判，最低价评审法，适用于技术复杂或紧急采购 / Competitive negotiation: multi-round separate negotiation with 2+ suppliers, lowest-price evaluation, suitable for complex technical or urgent procurement */
    NEGOTIATION(5, "竞争性谈判", "与2家及以上供应商分别多轮谈判，最低价评审法，适用于技术复杂或紧急采购"),

    /** 竞争性磋商：结构化流程+综合评分法，适用于科技创新、政府购买服务等复杂项目 / Competitive consultation: structured process + comprehensive scoring, suitable for tech innovation, government service procurement etc. */
    CONSULTATION(6, "竞争性磋商", "结构化流程+综合评分法，适用于科技创新、政府购买服务等复杂项目"),

    /** 单一来源采购：不经过竞争直接与唯一供应商商议，适用于专利技术、紧急情况、原项目配套 / Single-source: direct negotiation with the sole supplier, suitable for patented technology, emergency, or existing project supplements */
    SINGLE_SOURCE(7, "单一来源采购", "不经过竞争直接与唯一供应商商议，适用于专利技术、紧急情况、原项目配套"),

    /** 框架协议采购：公开程序入围→签订框架协议→实际需求时二次确定成交方，适用于多频次小额度采购 / Framework agreement: open shortlisting → sign framework → second-stage winner selection on actual demand, suitable for frequent small-amount procurement */
    FRAMEWORK(8, "框架协议采购", "公开程序入围→签订框架协议→实际需求时二次确定成交方，适用于多频次小额度采购"),

    /** 合作创新采购：邀请供应商合作研发创新产品，共担风险并承诺购买成果，适用于市场尚无成熟产品的战略项目 / Cooperative innovation: invite suppliers for collaborative R&D, shared risk with purchase commitment, suitable for strategic projects without mature market products */
    COLLABORATIVE(9, "合作创新采购", "邀请供应商合作研发创新产品，共担风险并承诺购买成果，适用于市场尚无成熟产品的战略项目");

    /** 采购方式编码（与数据库 procurement_type 字段对齐） / Procurement type code (aligned with DB procurement_type column) */
    private final int code;

    /** 采购方式中文名称 / Procurement type Chinese name */
    private final String label;

    /** 采购方式说明 / Procurement type description */
    private final String description;

    /**
     * 根据 code 获取枚举 / Get enum by code
     *
     * @param code 采购方式编码 / Procurement type code
     * @return 对应的枚举值，未匹配返回 null / Corresponding enum value, null if not matched
     */
    public static ProcurementTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProcurementTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
