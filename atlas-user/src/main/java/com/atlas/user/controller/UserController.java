package com.atlas.user.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.user.entity.Permission;
import com.atlas.user.entity.User;
import com.atlas.user.mapper.RoleMapper;
import com.atlas.user.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器 / User management controller
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户管理 / User Management")
public class UserController {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    @GetMapping("/page")
    @RequirePermission("system:user")
    public Result<Page<User>> page(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size) {
        return Result.ok(userMapper.selectPage(new Page<>(page, size), null));
    }

    @GetMapping("/{id}")
    @RequirePermission("system:user")
    public Result<User> getById(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail(1001, "用户不存在");
        }
        return Result.ok(user);
    }

    // ==================== 权限管理 / Permission Management ====================

    /**
     * 分配角色 — 全量替换用户角色 /
     * Assign roles — full replacement of user roles
     *
     * <pre>
     * PUT /api/user/{id}/roles
     * Body: { "roleIds": [1, 2, 3] }
     * </pre>
     */
    @PutMapping("/{id}/roles")
    @RequirePermission("system:user:role")
    @Transactional(rollbackFor = Exception.class)
    @Operation(summary = "分配角色 / Assign roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail(1001, "用户不存在");
        }

        List<Long> roleIds = body.get("roleIds");
        if (roleIds == null || roleIds.isEmpty()) {
            userMapper.deleteUserRoles(id);
            return Result.ok();
        }

        // 校验角色是否存在 / Validate roles exist
        for (Long roleId : roleIds) {
            if (roleMapper.selectById(roleId) == null) {
                return Result.fail(1005, "角色不存在: " + roleId);
            }
        }

        // 全量替换 / Full replacement
        userMapper.deleteUserRoles(id);
        userMapper.insertUserRoles(id, roleIds);
        return Result.ok();
    }

    /**
     * 查看用户权限详情 /
     * View user permission details
     *
     * <pre>
     * GET /api/user/{id}/permissions
     * Response: { "code": 200, "data": [ { "id": ..., "permCode": "...", ... } ] }
     * </pre>
     */
    @GetMapping("/{id}/permissions")
    @RequirePermission("system:user")
    @Operation(summary = "查看权限 / View permissions")
    public Result<List<Permission>> getPermissions(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.fail(1001, "用户不存在");
        }
        List<Permission> permissions = userMapper.selectPermissions(id);
        return Result.ok(permissions);
    }
}
