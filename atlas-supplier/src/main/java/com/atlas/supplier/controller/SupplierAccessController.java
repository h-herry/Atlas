package com.atlas.supplier.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.supplier.entity.RecruitNotice;
import com.atlas.supplier.entity.SupplierRegister;
import com.atlas.supplier.service.SupplierAccessService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 供应商准入管理 Controller — 招募公告 + 供应商注册 + 审批流转 /
 * Supplier access management Controller — recruitment notice + supplier registration + approval flow
 *
 * @author atlas
 */
@RestController
@RequestMapping("/api/supplier/access")
@RequiredArgsConstructor
@Tag(name = "供应商准入管理 / Supplier Access")
public class SupplierAccessController {

    private final SupplierAccessService accessService;

    // ==================== 招募公告 / Recruitment Notice ====================

    /** 发布招募公告 / Publish recruitment notice */
    @PostMapping("/notice")
    @RequirePermission("supplier:access:add")
    public Result<RecruitNotice> publishNotice(@RequestBody RecruitNotice notice) {
        return Result.ok(accessService.publishNotice(notice));
    }

    /** 分页查询招募公告 / Paginated query of recruitment notices */
    @GetMapping("/notice/page")
    @RequirePermission("supplier:access:view")
    public Result<Page<RecruitNotice>> pageNotice(@RequestParam(required = false) Integer status,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return Result.ok(accessService.pageNotice(status, page, size));
    }

    /** 关闭招募公告 / Close recruitment notice */
    @PutMapping("/notice/{id}/close")
    @RequirePermission("supplier:access:approve")
    public Result<Void> closeNotice(@PathVariable Long id) {
        accessService.closeNotice(id);
        return Result.ok();
    }

    // ==================== 供应商注册 / Supplier Registration ====================

    /** 供应商提交注册申请 / Supplier submits registration */
    @PostMapping("/register")
    @RequirePermission("supplier:access:add")
    public Result<SupplierRegister> submitRegister(@RequestBody SupplierRegister register) {
        return Result.ok(accessService.submitRegister(register));
    }

    /** 分页查询注册申请 / Paginated query of registrations */
    @GetMapping("/register/page")
    @RequirePermission("supplier:access:view")
    public Result<Page<SupplierRegister>> pageRegister(@RequestParam(required = false) Integer approvalStatus,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        return Result.ok(accessService.pageRegister(approvalStatus, page, size));
    }

    // ==================== 审批流转 / Approval Flow ====================

    /** 初审 / Initial review */
    @PostMapping("/register/{registerId}/initial-review")
    @RequirePermission("supplier:access:approve")
    public Result<Void> initialReview(@PathVariable Long registerId,
                                       @RequestParam Integer result,
                                       @RequestParam(required = false) BigDecimal score,
                                       @RequestParam(required = false) String comment,
                                       @RequestParam Long approverId,
                                       @RequestParam String approverName,
                                       @RequestParam(required = false, defaultValue = "QUALITY") String approverDept) {
        accessService.initialReview(registerId, result, score, comment, approverId, approverName, approverDept);
        return Result.ok();
    }

    /** 现场考察 / Field inspection */
    @PostMapping("/register/{registerId}/field-inspect")
    @RequirePermission("supplier:access:approve")
    public Result<Void> fieldInspect(@PathVariable Long registerId,
                                      @RequestParam Integer result,
                                      @RequestParam(required = false) BigDecimal score,
                                      @RequestParam(required = false) String comment,
                                      @RequestParam Long approverId,
                                      @RequestParam String approverName,
                                      @RequestParam(required = false, defaultValue = "QUALITY") String approverDept) {
        accessService.fieldInspect(registerId, result, score, comment, approverId, approverName, approverDept);
        return Result.ok();
    }

    /** 终审 / Final review */
    @PostMapping("/register/{registerId}/final-review")
    @RequirePermission("supplier:access:approve")
    public Result<Void> finalReview(@PathVariable Long registerId,
                                     @RequestParam Integer result,
                                     @RequestParam(required = false) BigDecimal score,
                                     @RequestParam(required = false) String comment,
                                     @RequestParam Long approverId,
                                     @RequestParam String approverName,
                                     @RequestParam(required = false, defaultValue = "QUALITY") String approverDept) {
        accessService.finalReview(registerId, result, score, comment, approverId, approverName, approverDept);
        return Result.ok();
    }
}
