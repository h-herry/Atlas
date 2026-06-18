package com.atlas.purchase.dto;

import lombok.Data;

import java.util.List;

/**
 * 邀请招标 - 邀请请求 DTO / Invited bidding - invite request DTO
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
public class InviteRequest {

    private List<Long> supplierIds;
    private List<String> supplierNames;
    private String invitationReason;
    private String bidEndDate;
    private String bidOpeningDate;
}
