package com.atlas.purchase.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.purchase.entity.AuctionBid;
import com.atlas.purchase.entity.AuctionPurchase;
import com.atlas.purchase.service.AuctionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 竞价采购 REST API / Auction procurement REST API
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/purchase/auction")
@RequiredArgsConstructor
@Tag(name = "竞价采购管理 / Auction Management")
public class AuctionController {

    private final AuctionService auctionService;

    /**
     * 分页查询竞价列表 / Paginated query of auction list
     */
    @GetMapping("/page")
    @RequirePermission("purchase:bidding:view")
    public Result<PageResult<AuctionPurchase>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<AuctionPurchase> page = auctionService.page(new Page<>(current, size), keyword, status);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询竞价详情 / Query auction detail
     */
    @GetMapping("/{id}")
    @RequirePermission("purchase:bidding:view")
    public Result<AuctionPurchase> getById(@PathVariable Long id) {
        return Result.success(auctionService.getById(id));
    }

    /**
     * 查询竞价出价记录 / Query auction bid records
     */
    @GetMapping("/{id}/bids")
    @RequirePermission("purchase:bidding:view")
    public Result<List<AuctionBid>> listBids(@PathVariable Long id) {
        return Result.success(auctionService.listBids(id));
    }

    /**
     * 启动竞价 / Start auction
     */
    @PutMapping("/{id}/start")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> start(@PathVariable Long id,
                               @RequestParam String endTime) {
        auctionService.start(id, java.time.LocalDateTime.parse(endTime));
        return Result.success();
    }

    /**
     * 提交出价 / Place bid
     */
    @PostMapping("/{id}/bid")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> placeBid(@PathVariable Long id,
                                  @RequestParam Long supplierId,
                                  @RequestParam String supplierName,
                                  @RequestParam java.math.BigDecimal bidAmount) {
        auctionService.placeBid(id, supplierId, supplierName, bidAmount);
        return Result.success();
    }

    /**
     * 结束竞价 / End auction
     */
    @PutMapping("/{id}/end")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> end(@PathVariable Long id) {
        auctionService.end(id);
        return Result.success();
    }

    /**
     * 定标 / Award
     */
    @PutMapping("/{id}/award")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> award(@PathVariable Long id) {
        auctionService.award(id);
        return Result.success();
    }

    /**
     * 终止竞价 / Terminate auction
     */
    @PutMapping("/{id}/terminate")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> terminate(@PathVariable Long id) {
        auctionService.terminate(id);
        return Result.success();
    }
}
