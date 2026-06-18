package com.atlas.purchase.inquiry.dto;

import com.atlas.purchase.inquiry.entity.Quote;
import com.atlas.purchase.inquiry.entity.QuoteItem;
import lombok.Data;

import java.util.List;

/**
 * 报价提交请求 DTO — 包装报价及明细 / Quote submission request DTO — wraps quote and its items
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
public class QuoteSubmitRequest {

    /** 报价主信息 / Quote header */
    private Quote quote;

    /** 报价明细列表 / Quote item list */
    private List<QuoteItem> items;
}
