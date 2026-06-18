package com.atlas.system.controller;

import com.atlas.common.core.web.Result;
import com.atlas.common.entity.SysRole;
import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.system.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器 — 系统角色的 CRUD 与权限配置 /
 * Role management controller — CRUD and permission configuration for system roles
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Tag(name = "角色管理 / Role Management", description = "系统角色的增删改查与权限分配")
@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 角色列表 / Role list
     */
    @GetMapping("/list")
    @RequirePermission("system:role:manage")
    @Operation(summary = "角色列表 / Role List", description = "查询所有启用角色 / Query all enabled roles")
    public Result<List<SysRole>> list() {
        return Result.ok(roleService.listAll());
    }

    /**
     * 角色详情 — 包含已分配的权限列表 / Role detail — includes assigned permission list
     */
    @GetMapping("/{id}")
    @RequirePermission("system:role:manage")
    @Operation(summary = "角色详情 / Role Detail", description = "查询角色详情及已分配权限列表 / Query role detail with assigned permissions")
    public Result<Map<String, Object>> getById(
            @Parameter(description = "角色ID / Role ID") @PathVariable Long id) {
        return Result.ok(roleService.getDetail(id));
    }

    /**
     * 创建角色 / Create role
     */
    @PostMapping
    @RequirePermission("system:role:manage")
    @Operation(summary = "创建角色 / Create Role", description = "新增一条角色记录 / Create a new role record")
    public Result<SysRole> create(@RequestBody SysRole role) {
        return Result.ok(roleService.create(role));
    }

    /**
     * 更新角色 / Update role
     */
    @PutMapping("/{id}")
    @RequirePermission("system:role:manage")
    @Operation(summary = "更新角色 / Update Role", description = "修改指定角色的信息 / Update specified role")
    public Result<SysRole> update(
            @Parameter(description = "角色ID / Role ID") @PathVariable Long id,
            @RequestBody SysRole role) {
        role.setId(id);
        return Result.ok(roleService.update(role));
    }

    /**
     * 删除角色 — 级联删除角色权限关联和用户角色关联 / Delete role — cascade delete mappings
     */
    @DeleteMapping("/{id}")
    @RequirePermission("system:role:manage")
    @Operation(summary = "删除角色 / Delete Role", description = "删除角色并级联清除权限/用户关联 / Delete role and cascade clear permission/user associations")
    public Result<Void> delete(
            @Parameter(description = "角色ID / Role ID") @PathVariable Long id) {
        roleService.delete(id);
        return Result.ok();
    }

    /**
     * 配置角色权限 — 全量替换角色的权限列表 / Configure role permissions — full replacement
     */
    @PostMapping("/{id}/permissions")
    @RequirePermission("system:role:manage")
    @Operation(summary = "配置角色权限 / Configure Role Permissions", description = "全量替换角色的权限列表 / Full replacement of role's permission list")
    public Result<Void> configurePermissions(
            @Parameter(description = "角色ID / Role ID") @PathVariable Long id,
            @RequestBody List<Long> permissionIds) {
        roleService.configurePermissions(id, permissionIds);
        return Result.ok();
    }
}
