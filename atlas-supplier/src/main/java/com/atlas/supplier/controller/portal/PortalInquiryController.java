package com.atlas.supplier.controller.portal;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.supplier.dto.portal.BidHistoryResponse;
import com.atlas.supplier.dto.portal.BidRequest;
import com.atlas.supplier.dto.portal.BiddingRoomResponse;
import com.atlas.supplier.dto.portal.ComparisonResultResponse;
import com.atlas.supplier.dto.portal.InquiryViewResponse;
import com.atlas.supplier.dto.portal.QuotationSubmitRequest;
import com.atlas.supplier.service.portal.PortalInquiryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 询报价管理控制器（供应商端） — 查看询价单、提交报价、查看中标结果 /
 * Inquiry & quotation management controller (portal) — view inquiries, submit quotes, view award results
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@RestController
@RequestMapping("/portal/inquiries")
@RequiredArgsConstructor
@Tag(name = "门户询价 / Portal Inquiry")
public class PortalInquiryController {

    private final PortalInquiryService portalInquiryService;

    /**
     * 查看企业发来的询价单列表（分页、状态筛选） / View inquiry list from enterprises (paginated, status filter)
     */
    @GetMapping
    @RequirePermission("supplier:portal:inquiry:view")
    public Result<Page<InquiryViewResponse>> listInquiries(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return Result.ok(portalInquiryService.listInquiries(page, size, status));
    }

    /**
     * 查看询价单详情（物料清单、要求交期、其他报价人匿名列表） /
     * View inquiry detail (material list, required delivery date, anonymous bidder list)
     */
    @GetMapping("/{id}")
    @RequirePermission("supplier:portal:inquiry:view")
    public Result<InquiryViewResponse> getInquiryDetail(@PathVariable Long id) {
        return Result.ok(portalInquiryService.getInquiryDetail(id));
    }

    /**
     * 提交报价（含物料单价、总价、交期、有效期） /
     * Submit quotation (unit price, total, delivery date, validity)
     */
    @PostMapping("/{id}/quote")
    @RequirePermission("supplier:portal:inquiry:quote")
    public Result<Void> submitQuote(@PathVariable Long id,
                                     @Valid @RequestBody QuotationSubmitRequest request) {
        portalInquiryService.submitQuote(id, request);
        return Result.ok();
    }

    /**
     * 修改报价（未截止前） / Modify quotation (before deadline)
     */
    @PutMapping("/{id}/quote")
    @RequirePermission("supplier:portal:inquiry:quote")
    public Result<Void> updateQuote(@PathVariable Long id,
                                     @Valid @RequestBody QuotationSubmitRequest request) {
        portalInquiryService.updateQuote(id, request);
        return Result.ok();
    }

    /**
     * 查看中标结果 / View award result
     */
    @GetMapping("/{id}/result")
    @RequirePermission("supplier:portal:inquiry:view")
    public Result<Map<String, Object>> getAwardResult(@PathVariable Long id) {
        return Result.ok(portalInquiryService.getAwardResult(id));
    }

    /**
     * 报价历史 / Quotation history
     */
    @GetMapping("/quotations/history")
    @RequirePermission("supplier:portal:inquiry:view")
    public Result<Page<Map<String, Object>>> getQuotationHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(portalInquiryService.getQuotationHistory(page, size));
    }

    // ==================== 竞价大厅 / Bidding Room ====================

    /**
     * 进入竞标大厅 — 返回当前排名（匿名）、我的报价、剩余时间、最低价 /
     * Enter bidding room — returns current ranking (anonymous), my bid, remaining time, lowest price
     */
    @GetMapping("/{id}/bidding-room")
    @RequirePermission("supplier:portal:inquiry:quote")
    public Result<BiddingRoomResponse> getBiddingRoomInfo(@PathVariable Long id) {
        return Result.ok(portalInquiryService.getBiddingRoomInfo(id));
    }

    /**
     * 提交竞价 — 校验报价低于最低价、在截止时间内、自动更新排名 /
     * Submit bid — validate price lower than current lowest, within deadline, auto-update ranking
     */
    @PostMapping("/{id}/bid")
    @RequirePermission("supplier:portal:inquiry:quote")
    public Result<Void> submitBid(@PathVariable Long id,
                                   @Valid @RequestBody BidRequest request) {
        portalInquiryService.submitBid(id, request);
        return Result.ok();
    }

    /**
     * 查看我的排名 — 实时查看当前供应商在所有竞价者中的排名 /
     * View my ranking — real-time ranking among all bidders
     */
    @GetMapping("/{id}/my-ranking")
    @RequirePermission("supplier:portal:inquiry:view")
    public Result<Map<String, Object>> getMyRanking(@PathVariable Long id) {
        BiddingRoomResponse roomInfo = portalInquiryService.getBiddingRoomInfo(id);
        Map<String, Object> ranking = new HashMap<>();
        ranking.put("inquiryId", id);
        ranking.put("myRank", roomInfo.getMyRank());
        ranking.put("myLastBid", roomInfo.getMyLastBid());
        ranking.put("currentLowestPrice", roomInfo.getCurrentLowestPrice());
        ranking.put("bidderCount", roomInfo.getBidderCount());
        ranking.put("totalBids", roomInfo.getTotalBids());
        return Result.ok(ranking);
    }

    /**
     * 查看比价结果 — 揭标后供应商可查看各供应商报价对比（供应商名匿名化） /
     * View comparison result — after opening, suppliers can view bid comparison (supplier names anonymized)
     */
    @GetMapping("/{id}/comparison")
    @RequirePermission("supplier:portal:inquiry:view")
    public Result<ComparisonResultResponse> getComparisonResult(@PathVariable Long id) {
        return Result.ok(portalInquiryService.getComparisonResult(id));
    }

    /**
     * 获取竞价出价历史 — 匿名化展示价格变化曲线 /
     * Get bid history — anonymized price change curve display
     */
    @GetMapping("/{id}/bid-history")
    @RequirePermission("supplier:portal:inquiry:view")
    public Result<BidHistoryResponse> getBidHistory(@PathVariable Long id) {
        return Result.ok(portalInquiryService.getBidHistory(id));
    }
}
