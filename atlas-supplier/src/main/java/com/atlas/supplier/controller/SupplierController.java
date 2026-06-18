package com.atlas.supplier.controller;

import com.atlas.common.annotation.AuditLog;
import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.supplier.entity.Supplier;
import com.atlas.supplier.service.SupplierService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 供应商管理 Controller / Supplier management Controller
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    /** 分页查询供应商 / Paginated query of suppliers */
    @GetMapping("/page")
    @RequirePermission("supplier:view")
    public Result<Page<Supplier>> page(@RequestParam(required = false) String keyword,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return Result.ok(supplierService.page(keyword, page, size));
    }

    /** 根据 ID 查询供应商 / Query supplier by ID */
    @GetMapping("/{id}")
    @RequirePermission("supplier:view")
    public Result<Supplier> get(@PathVariable Long id) {
        return Result.ok(supplierService.getById(id));
    }

    /** 新增供应商 / Add supplier */
    @PostMapping
    @RequirePermission("supplier:add")
    @AuditLog(module = "SUPPLIER", operation = "CREATE", description = "新增供应商 / Add supplier")
    public Result<Void> add(@Valid @RequestBody Supplier supplier) {
        supplierService.save(supplier);
        return Result.ok();
    }

    /** 更新供应商 / Update supplier */
    @PutMapping
    @RequirePermission("supplier:edit")
    public Result<Void> update(@Valid @RequestBody Supplier supplier) {
        supplierService.update(supplier);
        return Result.ok();
    }

    /** 删除供应商 / Delete supplier */
    @DeleteMapping("/{id}")
    @RequirePermission("supplier:del")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return Result.ok();
    }
}
