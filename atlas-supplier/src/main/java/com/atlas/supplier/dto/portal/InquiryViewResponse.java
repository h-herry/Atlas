package com.atlas.supplier.dto.portal;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 询价单视图响应 DTO（供应商端看到的） / Inquiry view response DTO (as seen by supplier)
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
public class InquiryViewResponse {

    /** 询价单ID / Inquiry ID */
    private Long id;

    /** 询价单编号 / Inquiry number */
    private String inquiryNo;

    /** 询价标题 / Inquiry title */
    private String title;

    /** 发布企业名称 / Publishing enterprise name */
    private String enterpriseName;

    /** 状态: OPEN 报价中 / CLOSED 已截止 / AWARDED 已定标 / Status */
    private String status;

    /** 物料清单 / Material list */
    private List<InquiryMaterialItem> materialItems;

    /** 要求交期 / Required delivery date */
    private LocalDateTime requiredDeliveryDate;

    /** 报价截止时间 / Quotation deadline */
    private LocalDateTime quotationDeadline;

    /** 其他报价人匿名列表（仅数量或匿名代号） / Anonymous list of other bidders (count or anonymous codes only) */
    private List<String> otherBidders;

    /** 已报价人数 / Number of bidders who submitted quotes */
    private Integer bidderCount;

    /** 发布时间 / Published time */
    private LocalDateTime publishedAt;

    // ==================== 内部类 / Inner Class ====================

    /**
     * 询价物料项 / Inquiry material item
     */
    @Data
    public static class InquiryMaterialItem {
        /** 物料编号 / Material code */
        private String materialCode;
        /** 物料名称 / Material name */
        private String materialName;
        /** 规格型号 / Specification */
        private String specification;
        /** 单位 / Unit */
        private String unit;
        /** 数量 / Quantity */
        private Integer quantity;
        /** 备注 / Remark */
        private String remark;
    }
}
