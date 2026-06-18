package com.atlas.common.security.service;

import com.atlas.common.entity.OrgStructure;
import com.atlas.common.entity.SysRole;
import com.atlas.common.entity.SysUserRole;
import com.atlas.common.mapper.OrgStructureMapper;
import com.atlas.common.mapper.SysRoleMapper;
import com.atlas.common.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据权限范围服务 — 基于组织架构计算用户可见的数据范围 /
 * Data Scope Service — computes user-visible data range based on organization structure
 * <p>
 * 核心规则 / Core Rules：
 * <ol>
 *   <li>查询用户所有角色，取最宽数据权限范围 (ALL > GROUP > DIVISION > PLANT > DEPT > SELF)</li>
 *   <li>根据范围返回可见的 org_node_id 集合</li>
 *   <li>ALL 返回空集（语义：不限制）</li>
 *   <li>SELF 返回空集（语义：通过 user_id 过滤）</li>
 * </ol>
 * <p>
 * 使用示例 / Usage：
 * <pre>{@code
 *   Set<Long> orgIds = dataScopeService.getDataScopeOrgIds(userId);
 *   if (orgIds == null) { // ALL — 不限制
 *       // no filter
 *   } else if (orgIds.isEmpty()) { // SELF — 仅本人
 *       // WHERE created_by = userId
 *   } else { // 其他范围
 *       // WHERE org_id IN (orgIds)
 *   }
 * }</pre>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataScopeService {

    /** 数据范围优先级：值越小范围越宽 / Data scope priority: smaller value = wider scope */
    private static final Map<String, Integer> SCOPE_PRIORITY = Map.of(
            "ALL", 0,
            "GROUP", 10,
            "DIVISION", 20,
            "PLANT", 30,
            "DEPT", 40,
            "SELF", 50
    );

    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final OrgStructureMapper orgStructureMapper;

    /**
     * 获取用户数据权限范围内的组织节点ID集合 /
     * Get the set of org node IDs within the user's data scope
     * <p>
     * 返回值语义 / Return value semantics：
     * <ul>
     *   <li>{@code null}  — ALL：不限制数据范围 / no data restriction</li>
     *   <li>空集合        — SELF：仅本人数据 / self data only</li>
     *   <li>非空集合      — 可见的组织节点ID列表 / visible org node IDs</li>
     * </ul>
     *
     * @param userId 用户ID / User ID
     * @return 可见组织节点ID集合，null 表示不限制 / Set of visible org node IDs, null means no restriction
     */
    public Set<Long> getDataScopeOrgIds(Long userId) {
        // 1. 查询用户所有角色 / Query all roles for the user
        List<SysRole> roles = sysRoleMapper.selectByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            log.warn("用户 {} 未分配任何角色，默认 SELF 范围 / User {} has no roles, defaulting to SELF scope", userId, userId);
            return Collections.emptySet();
        }

        // 2. 取最宽权限范围 / Choose the widest data scope
        String widestScope = roles.stream()
                .map(SysRole::getDataScope)
                .min(Comparator.comparingInt(s -> SCOPE_PRIORITY.getOrDefault(s, 99)))
                .orElse("SELF");

        log.debug("用户 {} 最宽数据范围={}, 角色数={} / User {} widest scope={}, role count={}",
                userId, widestScope, roles.size(), userId, widestScope, roles.size());

        // 3. 根据范围返回可见 org_node_id 集合 / Return visible org node IDs based on scope
        return switch (widestScope.toUpperCase()) {
            case "ALL" -> null;  // 全部数据，不限制 / All data, no restriction

            case "GROUP" -> getDescendantOrgIds(userId, "GROUP");

            case "DIVISION" -> getDescendantOrgIds(userId, "DIVISION");

            case "PLANT" -> getDescendantOrgIds(userId, "PLANT");

            case "DEPT" -> getUserDeptOrgIds(userId);

            case "SELF" -> Collections.emptySet();  // 仅本人，通过 user_id 过滤 / Self only, filter by user_id

            default -> {
                log.warn("未知的数据范围类型: {} / Unknown data scope type: {}", widestScope, widestScope);
                yield Collections.emptySet();
            }
        };
    }

    /**
     * 获取用户所属指定类型组织节点及其所有子孙节点ID /
     * Get the user's org node of given type and all its descendant IDs
     * <p>
     * 查找逻辑：从用户的 org_node_id 出发，沿 org_structure 树向上找到最近的指定类型祖先节点，
     * 然后返回该节点及其所有子孙节点 / Traverses up from the user's org_node_id to find the nearest
     * ancestor of the specified type, then returns that node and all descendants
     *
     * @param userId   用户ID / User ID
     * @param nodeType 目标节点类型 / Target node type (GROUP/DIVISION/PLANT)
     * @return 该类型节点及其所有子孙节点ID / The type-matching node and all descendant IDs
     */
    private Set<Long> getDescendantOrgIds(Long userId, String nodeType) {
        // 获取用户关联的组织节点 / Get user's associated org nodes
        List<SysUserRole> userRoles = sysUserRoleMapper.selectByUserId(userId);
        if (userRoles == null || userRoles.isEmpty()) {
            log.warn("用户 {} 没有组织关联 / User {} has no org association", userId, userId);
            return Collections.emptySet();
        }

        Set<Long> result = new HashSet<>();
        for (SysUserRole ur : userRoles) {
            if (ur.getOrgNodeId() == null) {
                continue;
            }
            OrgStructure node = orgStructureMapper.selectById(ur.getOrgNodeId());
            if (node == null) {
                continue;
            }

            // 沿树向上查找指定类型的祖先节点 / Traverse up to find ancestor of specified type
            OrgStructure targetNode = findAncestorByType(node, nodeType);
            if (targetNode == null) {
                log.debug("节点 {} 没有 {} 类型的祖先 / Node {} has no {} type ancestor",
                        node.getNodeId(), nodeType, node.getNodeId(), nodeType);
                continue;
            }

            // 查询该节点及其所有子孙 / Query this node and all descendants
            List<OrgStructure> descendants = orgStructureMapper.selectDescendants(targetNode.getNodePath());
            result.add(targetNode.getNodeId());
            descendants.forEach(d -> result.add(d.getNodeId()));
        }
        return result;
    }

    /**
     * 获取用户所属部门节点ID集合 / Get user's department org node IDs
     *
     * @param userId 用户ID / User ID
     * @return 部门节点ID集合 / Set of department node IDs
     */
    private Set<Long> getUserDeptOrgIds(Long userId) {
        List<SysUserRole> userRoles = sysUserRoleMapper.selectByUserId(userId);
        if (userRoles == null || userRoles.isEmpty()) {
            return Collections.emptySet();
        }

        return userRoles.stream()
                .map(SysUserRole::getOrgNodeId)
                .filter(Objects::nonNull)
                .flatMap(orgNodeId -> {
                    OrgStructure node = orgStructureMapper.selectById(orgNodeId);
                    if (node == null) return java.util.stream.Stream.empty();

                    // 沿树向上查找 DEPT 类型祖先 / Traverse up to find DEPT type ancestor
                    OrgStructure deptNode = findAncestorByType(node, "DEPT");
                    if (deptNode == null) {
                        // 如果没有 DEPT 类型祖先，使用节点本身 / If no DEPT ancestor, use the node itself
                        return java.util.stream.Stream.of(orgNodeId);
                    }

                    // 返回部门及其所有子孙节点 / Return department and all descendants
                    List<OrgStructure> descendants = orgStructureMapper.selectDescendants(deptNode.getNodePath());
                    Set<Long> ids = new HashSet<>();
                    ids.add(deptNode.getNodeId());
                    descendants.forEach(d -> ids.add(d.getNodeId()));
                    return ids.stream();
                })
                .collect(Collectors.toSet());
    }

    /**
     * 沿组织树向上查找指定类型的祖先节点 /
     * Traverse up the org tree to find an ancestor of the specified type
     *
     * @param node     起始节点 / Starting node
     * @param nodeType 目标节点类型 / Target node type
     * @return 匹配的祖先节点，未找到返回 null / Matching ancestor node, null if not found
     */
    private OrgStructure findAncestorByType(OrgStructure node, String nodeType) {
        // 如果当前节点就是目标类型 / If current node is already the target type
        if (nodeType.equalsIgnoreCase(node.getNodeType())) {
            return node;
        }

        // 沿 nodePath 向上逐级查找 / Traverse up via nodePath segments
        if (node.getNodePath() != null && !node.getNodePath().isEmpty()) {
            String[] pathSegments = node.getNodePath().split("/");
            // 从倒数第二段开始（跳过当前节点自身） / Start from second-last segment (skip self)
            for (int i = pathSegments.length - 2; i >= 1; i--) {
                String segment = pathSegments[i];
                if (segment.isEmpty()) continue;
                try {
                    Long ancestorId = Long.parseLong(segment);
                    OrgStructure ancestor = orgStructureMapper.selectById(ancestorId);
                    if (ancestor != null && nodeType.equalsIgnoreCase(ancestor.getNodeType())) {
                        return ancestor;
                    }
                } catch (NumberFormatException ignored) {
                    // skip non-numeric path segments
                }
            }
        }

        // 兜底：通过 parent_id 递归 / Fallback: recursive parent_id lookup
        OrgStructure current = node;
        int maxDepth = 10; // 防止无限循环 / Prevent infinite loop
        while (current.getParentId() != null && current.getParentId() > 0 && maxDepth-- > 0) {
            current = orgStructureMapper.selectById(current.getParentId());
            if (current == null) break;
            if (nodeType.equalsIgnoreCase(current.getNodeType())) {
                return current;
            }
        }

        return null;
    }
}
