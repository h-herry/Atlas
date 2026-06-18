package com.atlas.common.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 未读计数响应 DTO / Unread count response DTO
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountResponse {

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 未读消息总数 / Total unread messages */
    private Long totalUnread;

    /** 按类型统计未读数 / Unread count by type */
    private Long orderUnread;
    private Long deliveryUnread;
    private Long settlementUnread;
    private Long systemUnread;
    private Long approvalUnread;
    private Long qualityUnread;
}
