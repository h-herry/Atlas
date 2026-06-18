package com.atlas.system.service;

import com.atlas.common.entity.SysUserRole;
import com.atlas.common.mapper.SysUserRoleMapper;
import com.atlas.common.security.aspect.PermissionAspect;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户角色管理服务 — 用户与角色的分配、撤销与批量操作 /
 * User-role management service — assign, revoke and batch operations for user-role mappings
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final PermissionAspect permissionAspect;

    /**
     * 查询用户的所有角色关联 / Query all role assignments for a user
     *
     * @param userId 用户ID / User ID
     * @return 用户角色关联列表 / User-role mapping list
     */
    public List<SysUserRole> getByUserId(Long userId) {
        return sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        );
    }

    /**
     * 为用户分配角色 / Assign a role to a user
     * <p>
     * 使用 UNIQUE KEY uk_user_role_org 保证同一用户在同一组织节点下不重复分配同一角色 /
     * Uses UNIQUE KEY uk_user_role_org to prevent duplicate assignment of same role under same org node
     *
     * @param userRole 用户角色关联（含 userId, roleId, orgNodeId）/ User-role mapping (with userId, roleId, orgNodeId)
     * @return 保存后的关联 / Saved mapping
     */
    @Transactional(rollbackFor = Exception.class)
    public SysUserRole assign(SysUserRole userRole) {
        if (userRole.getUserId() == null || userRole.getRoleId() == null) {
            throw new IllegalArgumentException("用户ID和角色ID不能为空 / User ID and Role ID are required");
        }

        // 检查是否已存在 / Check if already exists
        Long exists = sysUserRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userRole.getUserId())
                        .eq(SysUserRole::getRoleId, userRole.getRoleId())
                        .eq(userRole.getOrgNodeId() != null,
                                SysUserRole::getOrgNodeId, userRole.getOrgNodeId())
                        .eq(userRole.getOrgNodeId() == null,
                                SysUserRole::getOrgNodeId, (Object) null) // MyBatis-Plus IS NULL
        );

        // 使用自定义 SQL 检查重复 / Use custom SQL to check duplicate
        List<SysUserRole> existingList = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userRole.getUserId())
                        .eq(SysUserRole::getRoleId, userRole.getRoleId())
        );
        boolean duplicate = existingList.stream().anyMatch(ur ->
                (userRole.getOrgNodeId() == null && ur.getOrgNodeId() == null) ||
                (userRole.getOrgNodeId() != null && userRole.getOrgNodeId().equals(ur.getOrgNodeId()))
        );

        if (duplicate) {
            log.warn("用户角色已存在: userId={}, roleId={}, orgNodeId={} / User-role already exists: userId={}, roleId={}, orgNodeId={}",
                    userRole.getUserId(), userRole.getRoleId(), userRole.getOrgNodeId(),
                    userRole.getUserId(), userRole.getRoleId(), userRole.getOrgNodeId());
            throw new RuntimeException("该用户在此组织节点下已拥有此角色 / User already has this role under this org node");
        }

        sysUserRoleMapper.insert(userRole);

        // 清除用户权限缓存 / Evict user permission cache
        permissionAspect.evictCache(userRole.getUserId());

        log.info("角色分配成功: userId={}, roleId={}, orgNodeId={} / Role assigned: userId={}, roleId={}, orgNodeId={}",
                userRole.getUserId(), userRole.getRoleId(), userRole.getOrgNodeId(),
                userRole.getUserId(), userRole.getRoleId(), userRole.getOrgNodeId());
        return userRole;
    }

    /**
     * 移除用户角色关联 / Remove a user-role mapping
     *
     * @param id 用户角色关联ID / User-role mapping ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        SysUserRole existing = sysUserRoleMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("用户角色关联不存在: id=" + id + " / User-role mapping not found: id=" + id);
        }

        sysUserRoleMapper.deleteById(id);

        // 清除用户权限缓存 / Evict user permission cache
        permissionAspect.evictCache(existing.getUserId());

        log.info("角色移除成功: id={}, userId={}, roleId={} / Role removed: id={}, userId={}, roleId={}",
                id, existing.getUserId(), existing.getRoleId(),
                id, existing.getUserId(), existing.getRoleId());
    }

    /**
     * 批量分配角色 — 全量替换用户在指定组织节点下的角色列表 /
     * Batch assign roles — full replacement of roles for a user under given org node
     * <p>
     * 操作步骤 / Steps：
     * <ol>
     *   <li>删除该用户在该组织节点下的所有现有角色 / Delete all existing roles for user under this org node</li>
     *   <li>批量插入新的角色关联 / Batch insert new role mappings</li>
     *   <li>清除用户权限缓存 / Evict user permission cache</li>
     * </ol>
     *
     * @param userId    用户ID / User ID
     * @param orgNodeId 组织节点ID（可为 null）/ Org node ID (nullable)
     * @param roleIds   角色ID列表 / Role ID list
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchAssign(Long userId, Long orgNodeId, List<Long> roleIds) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空 / User ID is required");
        }

        // 1. 删除现有角色 / Delete existing roles
        int deletedCount;
        if (orgNodeId != null) {
            deletedCount = sysUserRoleMapper.deleteByUserIdAndOrgNodeId(userId, orgNodeId);
        } else {
            // 删除该用户所有 org_node_id IS NULL 的角色 / Delete all roles where org_node_id IS NULL
            deletedCount = sysUserRoleMapper.delete(
                    new LambdaQueryWrapper<SysUserRole>()
                            .eq(SysUserRole::getUserId, userId)
                            .isNull(SysUserRole::getOrgNodeId)
            );
        }

        // 2. 批量插入新角色 / Batch insert new roles
        int insertedCount = 0;
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                ur.setOrgNodeId(orgNodeId);
                sysUserRoleMapper.insert(ur);
                insertedCount++;
            }
        }

        // 3. 清除缓存 / Evict cache
        permissionAspect.evictCache(userId);

        log.info("批量角色分配完成: userId={}, orgNodeId={}, 删除旧关联={}, 新增={} / Batch assign completed: userId={}, orgNodeId={}, deleted={}, inserted={}",
                userId, orgNodeId, deletedCount, insertedCount,
                userId, orgNodeId, deletedCount, insertedCount);
    }
}
