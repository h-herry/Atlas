package com.atlas.user.system.service;

import com.atlas.common.entity.SysPermission;
import com.atlas.common.entity.SysRole;
import com.atlas.common.entity.SysRolePermission;
import com.atlas.common.mapper.SysPermissionMapper;
import com.atlas.common.mapper.SysRoleMapper;
import com.atlas.common.mapper.SysRolePermissionMapper;
import com.atlas.common.mapper.SysUserRoleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色管理服务 — 系统角色的 CRUD 与权限配置 /
 * Role management service — CRUD and permission configuration for system roles
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 查询所有启用的角色列表 / Query all enabled roles
     *
     * @return 角色列表 / Role list
     */
    public List<SysRole> listAll() {
        return sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getStatus, 1)
                        .orderByAsc(SysRole::getId)
        );
    }

    /**
     * 查询角色详情 — 包含已分配的权限列表 /
     * Query role detail — includes assigned permission list
     *
     * @param roleId 角色ID / Role ID
     * @return 角色信息 + 权限列表 / Role info + permission list
     */
    public Map<String, Object> getDetail(Long roleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new RuntimeException("角色不存在: id=" + roleId + " / Role not found: id=" + roleId);
        }

        // 查询角色已分配的权限 / Query assigned permissions
        List<Long> permIds = sysRolePermissionMapper.selectPermissionIdsByRoleId(roleId);
        List<SysPermission> permissions = Collections.emptyList();
        if (permIds != null && !permIds.isEmpty()) {
            permissions = sysPermissionMapper.selectBatchIds(permIds);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("role", role);
        result.put("permissions", permissions);
        return result;
    }

    /**
     * 创建角色 / Create role
     *
     * @param role 角色实体 / Role entity
     * @return 保存后的角色 / Saved role
     */
    @Transactional(rollbackFor = Exception.class)
    public SysRole create(SysRole role) {
        if (role.getDataScope() == null || role.getDataScope().isEmpty()) {
            role.setDataScope("SELF");
        }
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        sysRoleMapper.insert(role);
        log.info("角色创建成功: code={}, name={}, dataScope={} / Role created: code={}, name={}, dataScope={}",
                role.getCode(), role.getName(), role.getDataScope(),
                role.getCode(), role.getName(), role.getDataScope());
        return role;
    }

    /**
     * 更新角色 / Update role
     *
     * @param role 角色实体（含ID）/ Role entity (with ID)
     * @return 更新后的角色 / Updated role
     */
    @Transactional(rollbackFor = Exception.class)
    public SysRole update(SysRole role) {
        SysRole existing = sysRoleMapper.selectById(role.getId());
        if (existing == null) {
            throw new RuntimeException("角色不存在: id=" + role.getId() + " / Role not found: id=" + role.getId());
        }
        sysRoleMapper.updateById(role);
        log.info("角色更新成功: id={}, code={} / Role updated: id={}, code={}",
                role.getId(), role.getCode(), role.getId(), role.getCode());
        return sysRoleMapper.selectById(role.getId());
    }

    /**
     * 删除角色 — 级联删除角色权限关联和用户角色关联 /
     * Delete role — cascade delete role-permission and user-role mappings
     *
     * @param roleId 角色ID / Role ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long roleId) {
        SysRole existing = sysRoleMapper.selectById(roleId);
        if (existing == null) {
            throw new RuntimeException("角色不存在: id=" + roleId + " / Role not found: id=" + roleId);
        }

        // 级联删除角色权限关联 / Cascade delete role-permission mappings
        int permMappings = sysRolePermissionMapper.deleteByRoleId(roleId);

        // 级联删除用户角色关联 / Cascade delete user-role mappings
        List<Long> userIds = sysUserRoleMapper.selectUserIdsByRoleId(roleId);
        // 使用 MyBatis-Plus 的 delete 方法删除所有关联的用户角色 / Delete all user-role mappings for this role
        sysUserRoleMapper.delete(new LambdaQueryWrapper<com.atlas.common.entity.SysUserRole>()
                .eq(com.atlas.common.entity.SysUserRole::getRoleId, roleId));

        sysRoleMapper.deleteById(roleId);

        log.info("角色删除成功: id={}, code={}, 级联清除权限关联={}, 受影响用户={} / Role deleted: id={}, code={}, perm mappings cleared={}, affected users={}",
                roleId, existing.getCode(), permMappings, userIds != null ? userIds.size() : 0,
                roleId, existing.getCode(), permMappings, userIds != null ? userIds.size() : 0);
    }

    /**
     * 配置角色权限 — 全量替换角色的权限列表 /
     * Configure role permissions — full replacement of role's permission list
     *
     * @param roleId        角色ID / Role ID
     * @param permissionIds 新的权限ID列表 / New permission ID list
     */
    @Transactional(rollbackFor = Exception.class)
    public void configurePermissions(Long roleId, List<Long> permissionIds) {
        SysRole existing = sysRoleMapper.selectById(roleId);
        if (existing == null) {
            throw new RuntimeException("角色不存在: id=" + roleId + " / Role not found: id=" + roleId);
        }

        // 1. 删除旧的权限关联 / Delete old permission mappings
        int deletedCount = sysRolePermissionMapper.deleteByRoleId(roleId);

        // 2. 批量插入新的权限关联 / Batch insert new permission mappings
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<SysRolePermission> mappings = permissionIds.stream()
                    .map(permId -> {
                        SysRolePermission rp = new SysRolePermission();
                        rp.setRoleId(roleId);
                        rp.setPermissionId(permId);
                        return rp;
                    })
                    .collect(Collectors.toList());

            // 批量插入 / Batch insert
            for (SysRolePermission mapping : mappings) {
                sysRolePermissionMapper.insert(mapping);
            }
        }

        log.info("角色权限配置成功: roleId={}, 旧关联删除={}, 新权限数={} / Role permissions configured: roleId={}, old deleted={}, new count={}",
                roleId, deletedCount, permissionIds != null ? permissionIds.size() : 0,
                roleId, deletedCount, permissionIds != null ? permissionIds.size() : 0);
    }
}
