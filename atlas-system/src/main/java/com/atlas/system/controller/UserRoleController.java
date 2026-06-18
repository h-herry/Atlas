package com.atlas.system.controller;

import com.atlas.common.core.web.Result;
import com.atlas.common.entity.SysUserRole;
import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.system.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户角色管理控制器 — 用户与角色的分配与撤销 /
 * User-role management controller — assign and revoke roles for users
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Tag(name = "用户角色管理 / User-Role Management", description = "用户角色分配、撤销与批量操作")
@RestController
@RequestMapping("/api/system/user-role")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    /**
     * 查询用户角色列表 / Query user role list
     */
    @GetMapping("/{userId}")
    @RequirePermission("system:user:manage")
    @Operation(summary = "用户角色列表 / User Role List", description = "查询指定用户的所有角色关联 / Query all role assignments for a user")
    public Result<List<SysUserRole>> getByUserId(
            @Parameter(description = "用户ID / User ID") @PathVariable Long userId) {
        return Result.ok(userRoleService.getByUserId(userId));
    }

    /**
     * 为用户分配角色 / Assign role to user
     */
    @PostMapping("/assign")
    @RequirePermission("system:user:manage")
    @Operation(summary = "分配角色 / Assign Role", description = "为指定用户分配角色，可指定组织节点 / Assign role to user with optional org node")
    public Result<SysUserRole> assign(@RequestBody SysUserRole userRole) {
        return Result.ok(userRoleService.assign(userRole));
    }

    /**
     * 移除用户角色 / Remove user role
     */
    @DeleteMapping("/{id}")
    @RequirePermission("system:user:manage")
    @Operation(summary = "移除角色 / Remove Role", description = "移除用户的指定角色关联 / Remove a role assignment from user")
    public Result<Void> remove(
            @Parameter(description = "用户角色关联ID / User-Role relation ID") @PathVariable Long id) {
        userRoleService.remove(id);
        return Result.ok();
    }

    /**
     * 批量分配角色 — 全量替换用户在指定组织节点下的角色 /
     * Batch assign roles — full replacement for a user under given org node
     */
    @PostMapping("/batch")
    @RequirePermission("system:user:manage")
    @Operation(summary = "批量分配 / Batch Assign", description = "全量替换用户在指定组织节点下的角色列表 / Full replacement of roles for a user under given org node")
    public Result<Void> batchAssign(@RequestBody BatchAssignRequest request) {
        userRoleService.batchAssign(request.getUserId(), request.getOrgNodeId(), request.getRoleIds());
        return Result.ok();
    }

    /**
     * 批量分配请求体 / Batch assign request body
     */
    @lombok.Data
    public static class BatchAssignRequest {
        private Long userId;
        private Long orgNodeId;
        private List<Long> roleIds;
    }
}
