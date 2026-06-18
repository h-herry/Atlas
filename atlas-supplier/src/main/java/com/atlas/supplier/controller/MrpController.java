package com.atlas.supplier.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.supplier.entity.MrpPlan;
import com.atlas.supplier.entity.MrpResult;
import com.atlas.supplier.service.MrpService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MRP 需求计划 Controller / MRP demand planning Controller
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/material/mrp")
@RequiredArgsConstructor
public class MrpController {

    private final MrpService mrpService;

    /** 创建 MRP 计划 / Create MRP plan */
    @PostMapping
    @RequirePermission("material:mrp:manage")
    public Result<MrpPlan> create(@RequestBody MrpPlan plan) {
        return Result.success(mrpService.create(plan));
    }

    /** 根据 ID 查询 MRP 计划 / Query MRP plan by ID */
    @GetMapping("/{id}")
    @RequirePermission("material:mrp:view")
    public Result<MrpPlan> getById(@PathVariable Long id) {
        return Result.success(mrpService.getById(id));
    }

    /** 分页查询 MRP 计划 / Paginated query of MRP plans */
    @GetMapping("/page")
    @RequirePermission("material:mrp:view")
    public Result<PageResult<MrpPlan>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MrpPlan> result = mrpService.page(keyword, status, page, size);
        return Result.success(PageResult.of(result));
    }

    /** 执行 MRP 计算 / Execute MRP calculation */
    @PostMapping("/{planId}/calculate")
    @RequirePermission("material:mrp:manage")
    public Result<Void> calculate(@PathVariable Long planId) {
        mrpService.calculate(planId);
        return Result.success();
    }

    /** 查询 MRP 计算结果 / List MRP calculation results */
    @GetMapping("/{planId}/results")
    @RequirePermission("material:mrp:view")
    public Result<List<MrpResult>> listResults(@PathVariable Long planId) {
        return Result.success(mrpService.listResults(planId));
    }

    /** 确认 MRP 计划 / Confirm MRP plan */
    @PutMapping("/{planId}/confirm")
    @RequirePermission("material:mrp:manage")
    public Result<Void> confirm(@PathVariable Long planId) {
        mrpService.confirm(planId);
        return Result.success();
    }

    /** 下发 MRP 计划 / Issue MRP plan */
    @PutMapping("/{planId}/issue")
    @RequirePermission("material:mrp:manage")
    public Result<Void> issue(@PathVariable Long planId) {
        mrpService.issue(planId);
        return Result.success();
    }
}
