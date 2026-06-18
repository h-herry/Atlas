package com.atlas.purchase.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.purchase.entity.FrameworkAgreement;
import com.atlas.purchase.entity.FrameworkOrder;
import com.atlas.purchase.entity.FrameworkSupplier;
import com.atlas.purchase.service.FrameworkService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 框架协议采购 REST API / Framework agreement procurement REST API
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/framework")
@RequiredArgsConstructor
public class FrameworkController {

    private final FrameworkService frameworkService;

    // ==================== 框架协议 / Framework Agreements ====================

    /**
     * 创建框架协议（草稿） / Create framework agreement (draft)
     */
    @PostMapping("/agreement")
    @RequirePermission("framework:add")
    public Result<FrameworkAgreement> createAgreement(@RequestBody FrameworkAgreement agreement) {
        return Result.ok(frameworkService.createAgreement(agreement));
    }

    /**
     * 开启入围征集 / Open shortlist solicitation
     */
    @PostMapping("/agreement/{agreementId}/collect")
    @RequirePermission("framework:manage")
    public Result<FrameworkAgreement> startCollection(@PathVariable Long agreementId) {
        return Result.ok(frameworkService.startCollection(agreementId));
    }

    /**
     * 入围评审 / Shortlist review
     */
    @PostMapping("/agreement/{agreementId}/review")
    @RequirePermission("framework:manage")
    public Result<FrameworkAgreement> review(@PathVariable Long agreementId) {
        return Result.ok(frameworkService.review(agreementId));
    }

    /**
     * 确定入围并设置入围供应商数 / Confirm shortlist and set shortlisted supplier count
     */
    @PostMapping("/agreement/{agreementId}/shortlist")
    @RequirePermission("framework:manage")
    public Result<FrameworkAgreement> confirmShortlist(@PathVariable Long agreementId) {
        return Result.ok(frameworkService.confirmShortlist(agreementId));
    }

    /**
     * 激活协议（协议生效） / Activate agreement (make effective)
     */
    @PostMapping("/agreement/{agreementId}/activate")
    @RequirePermission("framework:manage")
    public Result<FrameworkAgreement> activate(@PathVariable Long agreementId) {
        return Result.ok(frameworkService.activate(agreementId));
    }

    /**
     * 分页查询框架协议 / Paginated query of framework agreements
     */
    @GetMapping("/agreement/page")
    @RequirePermission("framework:view")
    public Result<PageResult<FrameworkAgreement>> pageAgreements(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<FrameworkAgreement> result = frameworkService.pageAgreements(keyword, status, page, size);
        return PageResult.ok(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    /**
     * 查询框架协议详情 / Query framework agreement detail
     */
    @GetMapping("/agreement/{agreementId}")
    @RequirePermission("framework:view")
    public Result<FrameworkAgreement> getAgreement(@PathVariable Long agreementId) {
        return Result.ok(frameworkService.getAgreement(agreementId));
    }

    // ==================== 供应商管理 / Supplier Management ====================

    /**
     * 添加入围供应商 / Add shortlisted supplier
     */
    @PostMapping("/supplier")
    @RequirePermission("framework:manage")
    public Result<FrameworkSupplier> addSupplier(@RequestBody FrameworkSupplier supplier) {
        return Result.ok(frameworkService.addSupplier(supplier));
    }

    /**
     * 查询协议下入围供应商列表 / Query shortlisted suppliers under agreement
     */
    @GetMapping("/agreement/{agreementId}/suppliers")
    @RequirePermission("framework:view")
    public Result<List<FrameworkSupplier>> listSuppliers(@PathVariable Long agreementId) {
        return Result.ok(frameworkService.listSuppliers(agreementId));
    }

    // ==================== 二次订单 / Secondary Orders ====================

    /**
     * 创建二次订单 / Create secondary order
     */
    @PostMapping("/order")
    @RequirePermission("framework:add")
    public Result<FrameworkOrder> createOrder(@RequestBody FrameworkOrder order) {
        return Result.ok(frameworkService.createOrder(order));
    }

    /**
     * 确认二次订单 / Confirm secondary order
     */
    @PostMapping("/order/{orderId}/confirm")
    @RequirePermission("framework:manage")
    public Result<FrameworkOrder> confirmOrder(@PathVariable Long orderId) {
        return Result.ok(frameworkService.confirmOrder(orderId));
    }

    /**
     * 完成履约 / Complete fulfillment
     */
    @PostMapping("/order/{orderId}/complete")
    @RequirePermission("framework:manage")
    public Result<FrameworkOrder> completeOrder(@PathVariable Long orderId) {
        return Result.ok(frameworkService.completeOrder(orderId));
    }

    /**
     * 分页查询二次订单 / Paginated query of secondary orders
     */
    @GetMapping("/order/page")
    @RequirePermission("framework:view")
    public Result<PageResult<FrameworkOrder>> pageOrders(
            @RequestParam(required = false) Long agreementId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<FrameworkOrder> result = frameworkService.pageOrders(agreementId, status, page, size);
        return PageResult.ok(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }
}
