package com.atlas.purchase.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.purchase.entity.BiddingHall;
import com.atlas.purchase.entity.BiddingHallRecord;
import com.atlas.purchase.entity.PriceLibrary;
import com.atlas.purchase.entity.PriceTrend;
import com.atlas.purchase.entity.SupplierRecommendation;
import com.atlas.purchase.service.BiddingHallService;
import com.atlas.purchase.service.PriceLibraryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 竞价大厅 REST API / Bidding hall REST API
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@RestController
@RequestMapping("/api/bidding-hall")
@RequiredArgsConstructor
@Tag(name = "招标大厅 / Bidding Hall")
public class BiddingHallController {

    private final BiddingHallService hallService;

    /**
     * 分页查询竞价大厅 / Paginated query of bidding halls
     */
    @GetMapping("/page")
    @RequirePermission("purchase:view")
    public Result<Page<BiddingHall>> page(@RequestParam(required = false) Integer status,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.ok(hallService.page(status, page, size));
    }

    /**
     * 开启竞价大厅 / Open bidding hall
     */
    @PostMapping("/open")
    @RequirePermission("purchase:add")
    public Result<BiddingHall> openHall(@RequestParam Long auctionId,
                                         @RequestParam(defaultValue = "ENGLISH") String biddingStyle,
                                         @RequestParam(defaultValue = "false") boolean identityHidden,
                                         @RequestParam(defaultValue = "true") boolean rankPublic,
                                         @RequestParam(defaultValue = "0") int autoExtendSeconds,
                                         @RequestParam String startTime,
                                         @RequestParam String endTime) {
        return Result.ok(hallService.openHall(auctionId, biddingStyle, identityHidden,
                rankPublic, autoExtendSeconds,
                java.time.LocalDateTime.parse(startTime), java.time.LocalDateTime.parse(endTime)));
    }

    /**
     * 提交报价 / Submit bid
     */
    @PostMapping("/{hallId}/bid")
    @RequirePermission("purchase:edit")
    public Result<Integer> submitBid(@PathVariable Long hallId,
                                      @RequestParam Long supplierId,
                                      @RequestParam BigDecimal bidAmount) {
        return Result.ok(hallService.submitBid(hallId, supplierId, bidAmount));
    }

    /**
     * 获取实时排名 / Get real-time rankings
     */
    @GetMapping("/{hallId}/rankings")
    @RequirePermission("purchase:view")
    public Result<List<BiddingHallRecord>> getRankings(@PathVariable Long hallId) {
        return Result.ok(hallService.getRankings(hallId));
    }

    /**
     * 关闭竞价大厅 / Close bidding hall
     */
    @PutMapping("/{hallId}/close")
    @RequirePermission("purchase:edit")
    public Result<Void> closeHall(@PathVariable Long hallId) {
        hallService.closeHall(hallId);
        return Result.ok();
    }

    /**
     * 暂停竞价 / Pause bidding
     */
    @PutMapping("/{hallId}/pause")
    @RequirePermission("purchase:edit")
    public Result<Void> pauseHall(@PathVariable Long hallId) {
        hallService.pauseHall(hallId);
        return Result.ok();
    }

    /**
     * 禁用供应商 / Ban supplier
     */
    @PostMapping("/{hallId}/ban")
    @RequirePermission("purchase:edit")
    public Result<Void> banSupplier(@PathVariable Long hallId, @RequestParam Long supplierId) {
        hallService.banSupplier(hallId, supplierId);
        return Result.ok();
    }
}

/**
 * 价格库 REST API / Price library REST API
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@RestController
@RequestMapping("/api/price-library")
@RequiredArgsConstructor
class PriceLibraryController {

    private final PriceLibraryService priceService;

    /**
     * 分页查询价格库 / Paginated query of price library
     */
    @GetMapping("/page")
    @RequirePermission("purchase:view")
    public Result<Page<PriceLibrary>> page(@RequestParam(required = false) Long materialId,
                                            @RequestParam(required = false) Long supplierId,
                                            @RequestParam(required = false) String priceType,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(priceService.page(materialId, supplierId, priceType, page, size));
    }

    /**
     * 添加价格记录 / Add price record
     */
    @PostMapping
    @RequirePermission("purchase:add")
    public Result<Void> add(@RequestBody PriceLibrary price) {
        priceService.addPrice(price);
        return Result.ok();
    }

    /**
     * 作废价格 / Invalidate price
     */
    @PutMapping("/{id}/invalidate")
    @RequirePermission("purchase:edit")
    public Result<Void> invalidate(@PathVariable Long id) {
        priceService.invalidatePrice(id);
        return Result.ok();
    }

    /**
     * 价格对比（取最低价） / Price comparison (lowest price)
     */
    @GetMapping("/compare/{materialId}")
    @RequirePermission("purchase:view")
    public Result<PriceLibrary> compareLowest(@PathVariable Long materialId) {
        return priceService.compareLowestPrice(materialId)
                .map(Result::ok)
                .orElse(Result.fail(com.atlas.common.core.enums.ErrorCode.PRICE_NOT_EXIST));
    }

    /**
     * 价格走势查询 / Price trend query
     */
    @GetMapping("/trend/{materialId}")
    @RequirePermission("purchase:view")
    public Result<List<PriceTrend>> trend(@PathVariable Long materialId) {
        return Result.ok(priceService.getTrend(materialId));
    }

    /**
     * 重新计算价格走势 / Recalculate price trend
     */
    @PostMapping("/trend/recalculate")
    @RequirePermission("purchase:edit")
    public Result<Void> recalculateTrend(@RequestParam Long materialId,
                                          @RequestParam String period) {
        priceService.recalculateTrend(materialId, period);
        return Result.ok();
    }

    /**
     * 供应商推荐 / Supplier recommendation
     */
    @GetMapping("/recommend/{materialId}")
    @RequirePermission("purchase:view")
    public Result<List<SupplierRecommendation>> recommend(@PathVariable Long materialId) {
        return Result.ok(priceService.recommendSuppliers(materialId));
    }

    /**
     * 添加推荐记录 / Add recommendation record
     */
    @PostMapping("/recommend")
    @RequirePermission("purchase:add")
    public Result<Void> addRecommendation(@RequestBody SupplierRecommendation rec) {
        priceService.addRecommendation(rec);
        return Result.ok();
    }
}
