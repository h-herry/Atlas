package com.atlas.purchase.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.purchase.entity.OpenBidding;
import com.atlas.purchase.entity.OpenBiddingSupplier;
import com.atlas.purchase.service.OpenBiddingService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公开招标 Controller / Open Bidding Controller
 * <p>
 * 提供公开招标全流程 API：发布 → 招标 → 投标 → 开标 → 评标 → 定标。 /
 * Provides full open bidding lifecycle API: publish → bid → submit → open → evaluate → award.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/purchase/open-bidding")
@RequiredArgsConstructor
public class OpenBiddingController {

    private final OpenBiddingService openBiddingService;

    /**
     * 分页查询招标项目 / Paginated query of bidding projects
     */
    @GetMapping("/page")
    @RequirePermission("purchase:bidding:view")
    public Result<PageResult<OpenBidding>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<OpenBidding> page = openBiddingService.page(new Page<>(current, size), keyword, status);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询招标详情 / Query bidding detail
     */
    @GetMapping("/{id}")
    @RequirePermission("purchase:bidding:view")
    public Result<OpenBidding> getById(@PathVariable Long id) {
        return Result.success(openBiddingService.getById(id));
    }

    /**
     * 查询招标参与的供应商列表 / Query participating suppliers
     */
    @GetMapping("/{id}/suppliers")
    @RequirePermission("purchase:bidding:view")
    public Result<List<OpenBiddingSupplier>> listSuppliers(@PathVariable Long id) {
        return Result.success(openBiddingService.listSuppliers(id));
    }

    /**
     * 发布招标公告 / Publish bidding announcement
     */
    @PutMapping("/{id}/publish")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> publish(@PathVariable Long id,
                                 @RequestParam Long publisherId,
                                 @RequestParam String bidEndDate,
                                 @RequestParam String bidOpeningDate,
                                 @RequestParam(required = false) String bidContent) {
        openBiddingService.publish(id, publisherId,
            java.time.LocalDate.parse(bidEndDate),
            java.time.LocalDate.parse(bidOpeningDate),
            bidContent);
        return Result.success();
    }

    /**
     * 开始招标 / Start bidding
     */
    @PutMapping("/{id}/start-bidding")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> startBidding(@PathVariable Long id) {
        openBiddingService.startBidding(id);
        return Result.success();
    }

    /**
     * 提交投标 / Submit bid
     */
    @PostMapping("/{id}/submit-bid")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> submitBid(@PathVariable Long id,
                                   @RequestParam Long supplierId,
                                   @RequestParam String supplierName,
                                   @RequestParam java.math.BigDecimal bidAmount,
                                   @RequestParam(required = false) String bidFileUrl) {
        openBiddingService.submitBid(id, supplierId, supplierName, bidAmount, bidFileUrl);
        return Result.success();
    }

    /**
     * 开标 / Open bid
     */
    @PutMapping("/{id}/open-bid")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> openBid(@PathVariable Long id) {
        openBiddingService.openBid(id);
        return Result.success();
    }

    /**
     * 开始评标 / Start evaluation
     */
    @PutMapping("/{id}/start-evaluation")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> startEvaluation(@PathVariable Long id) {
        openBiddingService.startEvaluation(id);
        return Result.success();
    }

    /**
     * 评审供应商 / Evaluate supplier
     */
    @PutMapping("/supplier/{supplierRecordId}/evaluate")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> evaluate(@PathVariable Long supplierRecordId,
                                  @RequestParam java.math.BigDecimal score,
                                  @RequestParam(required = false) String comment) {
        openBiddingService.evaluate(supplierRecordId, score, comment, supplierRecordId);
        return Result.success();
    }

    /**
     * 定标 / Award contract
     */
    @PutMapping("/{id}/award")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> award(@PathVariable Long id) {
        openBiddingService.award(id);
        return Result.success();
    }

    /**
     * 流标 / Flow bid (no winner)
     */
    @PutMapping("/{id}/flow-bid")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> flowBid(@PathVariable Long id) {
        openBiddingService.flowBid(id);
        return Result.success();
    }

    /**
     * 终止招标 / Terminate bidding
     */
    @PutMapping("/{id}/terminate")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> terminate(@PathVariable Long id) {
        openBiddingService.terminate(id);
        return Result.success();
    }
}
