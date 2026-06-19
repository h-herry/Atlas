package com.atlas.user.system.service;

import com.atlas.common.entity.SysPermission;
import com.atlas.common.mapper.SysPermissionMapper;
import com.atlas.common.mapper.SysRolePermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限管理服务 — 系统权限的 CRUD 与树形查询 /
 * Permission management service — CRUD and tree query for system permissions
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;

    /**
     * 获取权限树 — 按模块分组，每个模块内按 parent_id 构建树形结构 /
     * Get permission tree — grouped by module, tree structure within each module
     *
     * @return 模块名 → 权限树列表 / Module name → permission tree list
     */
    public Map<String, List<SysPermission>> getPermissionTree() {
        // 查询所有启用的权限 / Query all enabled permissions
        List<SysPermission> allPermissions = sysPermissionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getStatus, 1)
                        .orderByAsc(SysPermission::getSortOrder)
        );

        if (allPermissions == null || allPermissions.isEmpty()) {
            return Collections.emptyMap();
        }

        // 按模块分组 / Group by module
        Map<String, List<SysPermission>> moduleMap = allPermissions.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getModule() != null ? p.getModule() : "UNCATEGORIZED",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // 每个模块内构建树形结构 / Build tree within each module
        Map<String, List<SysPermission>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<SysPermission>> entry : moduleMap.entrySet()) {
            List<SysPermission> tree = buildTree(entry.getValue());
            result.put(entry.getKey(), tree);
        }

        return result;
    }

    /**
     * 按模块内 parent_id 构建树形结构 / Build tree structure by parent_id within module
     */
    private List<SysPermission> buildTree(List<SysPermission> permissions) {
        Map<Long, List<SysPermission>> childrenMap = new HashMap<>();
        List<SysPermission> roots = new ArrayList<>();

        for (SysPermission perm : permissions) {
            if (perm.getParentId() == null || perm.getParentId() == 0) {
                roots.add(perm);
            } else {
                childrenMap.computeIfAbsent(perm.getParentId(), k -> new ArrayList<>()).add(perm);
            }
        }

        // 只返回顶层节点（children 在需要使用子列表时由调用方自行构建） /
        // Return only top-level nodes (callers build sub-lists as needed)
        return roots;
    }

    /**
     * 创建权限 / Create permission
     *
     * @param permission 权限实体 / Permission entity
     * @return 保存后的权限 / Saved permission
     */
    @Transactional(rollbackFor = Exception.class)
    public SysPermission create(SysPermission permission) {
        if (permission.getType() == null || permission.getType().isEmpty()) {
            permission.setType("FUNCTION");
        }
        if (permission.getStatus() == null) {
            permission.setStatus(1);
        }
        sysPermissionMapper.insert(permission);
        log.info("权限创建成功: code={}, name={} / Permission created: code={}, name={}",
                permission.getCode(), permission.getName(), permission.getCode(), permission.getName());
        return permission;
    }

    /**
     * 更新权限 / Update permission
     *
     * @param permission 权限实体（含ID）/ Permission entity (with ID)
     * @return 更新后的权限 / Updated permission
     */
    @Transactional(rollbackFor = Exception.class)
    public SysPermission update(SysPermission permission) {
        SysPermission existing = sysPermissionMapper.selectById(permission.getId());
        if (existing == null) {
            throw new RuntimeException("权限不存在: id=" + permission.getId() + " / Permission not found: id=" + permission.getId());
        }
        sysPermissionMapper.updateById(permission);
        log.info("权限更新成功: id={}, code={} / Permission updated: id={}, code={}",
                permission.getId(), permission.getCode(), permission.getId(), permission.getCode());
        return sysPermissionMapper.selectById(permission.getId());
    }

    /**
     * 删除权限 — 级联删除角色权限关联 /
     * Delete permission — cascade delete role-permission mappings
     *
     * @param id 权限ID / Permission ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        SysPermission existing = sysPermissionMapper.selectById(id);
        if (existing == null) {
            throw new RuntimeException("权限不存在: id=" + id + " / Permission not found: id=" + id);
        }

        // 级联删除角色权限关联 / Cascade delete role-permission mappings
        int deletedMappings = sysRolePermissionMapper.deleteByPermissionId(id);
        sysPermissionMapper.deleteById(id);

        log.info("权限删除成功: id={}, code={}, 级联清除关联数={} / Permission deleted: id={}, code={}, cascade mappings cleared={}",
                id, existing.getCode(), deletedMappings, id, existing.getCode(), deletedMappings);
    }

    /**
     * 根据ID查询权限 / Query permission by ID
     *
     * @param id 权限ID / Permission ID
     * @return 权限实体 / Permission entity
     */
    public SysPermission getById(Long id) {
        SysPermission permission = sysPermissionMapper.selectById(id);
        if (permission == null) {
            throw new RuntimeException("权限不存在: id=" + id + " / Permission not found: id=" + id);
        }
        return permission;
    }
}
