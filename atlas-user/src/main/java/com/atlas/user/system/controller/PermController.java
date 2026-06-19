package com.atlas.user.system.controller;

import com.atlas.common.core.web.Result;
import com.atlas.common.entity.SysPermission;
import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.user.system.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 权限管理控制器 — 系统权限的 CRUD 与树形查询 /
 * Permission management controller — CRUD and tree query for system permissions
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Tag(name = "权限管理 / Permission Management", description = "系统权限的增删改查与树形查询")
@RestController
@RequestMapping("/api/system/perm")
@RequiredArgsConstructor
public class PermController {

    private final PermissionService permissionService;

    /**
     * 获取权限树 — 按模块分组，每个模块下按 parent_id 构建树形结构 /
     * Get permission tree — grouped by module, tree structure within each module
     */
    @GetMapping("/tree")
    @RequirePermission("system:perm:view")
    @Operation(summary = "权限树 / Permission Tree", description = "按模块分组返回完整权限树 / Returns complete permission tree grouped by module")
    public Result<Map<String, List<SysPermission>>> tree() {
        return Result.ok(permissionService.getPermissionTree());
    }

    /**
     * 创建权限 / Create permission
     */
    @PostMapping
    @RequirePermission("system:perm:manage")
    @Operation(summary = "创建权限 / Create Permission", description = "新增一条权限记录 / Create a new permission record")
    public Result<SysPermission> create(@RequestBody SysPermission permission) {
        return Result.ok(permissionService.create(permission));
    }

    /**
     * 更新权限 / Update permission
     */
    @PutMapping("/{id}")
    @RequirePermission("system:perm:manage")
    @Operation(summary = "更新权限 / Update Permission", description = "修改指定权限的信息 / Update specified permission")
    public Result<SysPermission> update(
            @Parameter(description = "权限ID / Permission ID") @PathVariable Long id,
            @RequestBody SysPermission permission) {
        permission.setId(id);
        return Result.ok(permissionService.update(permission));
    }

    /**
     * 删除权限 — 级联删除角色关联 / Delete permission — cascade delete role associations
     */
    @DeleteMapping("/{id}")
    @RequirePermission("system:perm:manage")
    @Operation(summary = "删除权限 / Delete Permission", description = "删除权限并级联清除角色关联 / Delete permission and cascade clear role associations")
    public Result<Void> delete(
            @Parameter(description = "权限ID / Permission ID") @PathVariable Long id) {
        permissionService.delete(id);
        return Result.ok();
    }

    /**
     * 查询单个权限 / Get single permission
     */
    @GetMapping("/{id}")
    @RequirePermission("system:perm:view")
    @Operation(summary = "权限详情 / Permission Detail", description = "根据ID查询权限 / Query permission by ID")
    public Result<SysPermission> getById(
            @Parameter(description = "权限ID / Permission ID") @PathVariable Long id) {
        return Result.ok(permissionService.getById(id));
    }
}
