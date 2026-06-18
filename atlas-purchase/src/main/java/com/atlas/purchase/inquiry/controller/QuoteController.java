package com.atlas.purchase.inquiry.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.purchase.inquiry.dto.QuoteSubmitRequest;
import com.atlas.purchase.inquiry.entity.Quote;
import com.atlas.purchase.inquiry.entity.QuoteItem;
import com.atlas.purchase.inquiry.service.QuoteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报价 REST API / Quote REST API
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@RestController
@RequestMapping("/api/quote")
@RequiredArgsConstructor
@Tag(name = "报价管理 / Quote Management")
public class QuoteController {

    private final QuoteService quoteService;

    /**
     * 供应商报价 / Supplier submits quote
     */
    @PostMapping
    @RequirePermission("purchase:add")
    public Result<Quote> submit(@RequestBody QuoteSubmitRequest request) {
        return Result.ok(quoteService.submit(request.getQuote(),
                request.getItems() != null ? request.getItems() : List.of()));
    }

    /**
     * 修改报价 / Update quote
     */
    @PutMapping("/{id}")
    @RequirePermission("purchase:edit")
    public Result<Quote> update(@PathVariable Long id, @RequestBody QuoteSubmitRequest request) {
        return Result.ok(quoteService.updateQuote(id, request.getQuote(),
                request.getItems() != null ? request.getItems() : List.of()));
    }

    /**
     * 某询价下所有报价 / All quotes for an inquiry
     */
    @GetMapping("/{inquiryId}/list")
    @RequirePermission("purchase:view")
    public Result<List<Quote>> listByInquiry(@PathVariable Long inquiryId) {
        return Result.ok(quoteService.listByInquiry(inquiryId));
    }

    /**
     * 撤回报价 / Withdraw quote
     */
    @PutMapping("/{id}/withdraw")
    @RequirePermission("purchase:edit")
    public Result<Void> withdraw(@PathVariable Long id) {
        quoteService.withdraw(id);
        return Result.ok();
    }

    /**
     * 报价明细 / Quote items
     */
    @GetMapping("/{id}/items")
    @RequirePermission("purchase:view")
    public Result<List<QuoteItem>> items(@PathVariable Long id) {
        return Result.ok(quoteService.listItems(id));
    }
}
