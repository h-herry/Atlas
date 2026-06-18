package com.atlas.purchase.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.purchase.entity.CooperativeInnovation;
import com.atlas.purchase.service.CooperativeInnovationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 合作创新采购 REST API / Cooperative innovation procurement REST API
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/purchase/cooperative-innovation")
@RequiredArgsConstructor
public class CooperativeInnovationController {

    private final CooperativeInnovationService cooperativeInnovationService;

    /**
     * 分页查询合作创新 / Paginated query of cooperative innovations
     */
    @GetMapping("/page")
    @RequirePermission("purchase:bidding:view")
    public Result<PageResult<CooperativeInnovation>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<CooperativeInnovation> page = cooperativeInnovationService.page(new Page<>(current, size), keyword, status);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询合作创新详情 / Query cooperative innovation detail
     */
    @GetMapping("/{id}")
    @RequirePermission("purchase:bidding:view")
    public Result<CooperativeInnovation> getById(@PathVariable Long id) {
        return Result.success(cooperativeInnovationService.getById(id));
    }

    /**
     * 研发评审 / R&D review
     */
    @PutMapping("/{id}/review")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> startReview(@PathVariable Long id,
                                     @RequestParam String rdContent,
                                     @RequestParam String rdCycle,
                                     @RequestParam BigDecimal rdBudget,
                                     @RequestParam Long partnerSupplierId) {
        cooperativeInnovationService.startReview(id, rdContent, rdCycle, rdBudget, partnerSupplierId);
        return Result.success();
    }

    /**
     * 开始合作 / Start cooperation
     */
    @PutMapping("/{id}/cooperate")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> startCooperating(@PathVariable Long id) {
        cooperativeInnovationService.startCooperating(id);
        return Result.success();
    }

    /**
     * 成果验收 / Achievement acceptance
     */
    @PutMapping("/{id}/accept")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> startAcceptance(@PathVariable Long id,
                                         @RequestParam(required = false) String stageProgress) {
        cooperativeInnovationService.startAcceptance(id, stageProgress);
        return Result.success();
    }

    /**
     * 完成合作 / Complete cooperation
     */
    @PutMapping("/{id}/complete")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> complete(@PathVariable Long id) {
        cooperativeInnovationService.complete(id);
        return Result.success();
    }

    /**
     * 终止合作 / Terminate cooperation
     */
    @PutMapping("/{id}/terminate")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> terminate(@PathVariable Long id) {
        cooperativeInnovationService.terminate(id);
        return Result.success();
    }
}
