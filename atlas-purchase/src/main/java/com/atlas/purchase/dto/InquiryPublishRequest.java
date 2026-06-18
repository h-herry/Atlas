package com.atlas.purchase.dto;

import lombok.Data;

import java.util.List;

/**
 * 询比采购 - 发布请求 DTO / Inquiry procurement - publish request DTO
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
public class InquiryPublishRequest {

    private List<Long> supplierIds;
    private List<String> supplierNames;
    private String inquiryContent;
    private String deadline;
}
