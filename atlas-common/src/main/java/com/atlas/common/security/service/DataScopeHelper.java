package com.atlas.common.security.service;

import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限 SQL 条件构建工具 / Data Scope SQL Condition Builder
 * <p>
 * 根据数据权限计算结果生成 WHERE 子句片段，用于拼接到 MyBatis Mapper SQL 中 /
 * Generates WHERE clause fragments based on data scope computation results,
 * used to append filtering conditions in MyBatis Mapper SQL
 * <p>
 * 典型用法 / Typical Usage：
 * <pre>{@code
 *   // Controller / Service 层
 *   Set<Long> orgIds = dataScopeService.getDataScopeOrgIds(userId);
 *   String scopeCondition = DataScopeHelper.buildScopeCondition("o", "created_by", orgIds);
 *   // Mapper 接收 condition 并拼接到 SQL
 * }</pre>
 * <p>
 * 生成示例 / Generated examples：
 * <ul>
 *   <li>ALL  → "" (空字符串，不添加条件) / empty string, no condition</li>
 *   <li>SELF → " AND (o.created_by = 1001)" </li>
 *   <li>DEPT → " AND (o.org_id IN (10, 11, 12))" </li>
 * </ul>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
public final class DataScopeHelper {

    private DataScopeHelper() {
        // 工具类禁止实例化 / Utility class, prevent instantiation
    }

    /**
     * 构造数据权限 SQL 过滤条件 / Build data scope SQL filter condition
     *
     * @param alias         表别名（如 "o"）/ Table alias (e.g., "o")
     * @param userIdColumn  用户ID字段名（如 "created_by"）/ User ID column name (e.g., "created_by")
     * @param orgIds        可见组织节点ID集合 / Set of visible org node IDs
     *                      — null: ALL，不限制 / ALL, no filter
     *                      — 空集合: SELF，仅本人 / SELF, current user only
     *                      — 非空: 按组织节点过滤 / Filter by org node IDs
     * @return SQL 条件片段（以 " AND " 开头） / SQL condition fragment (prefixed with " AND ")
     */
    public static String buildScopeCondition(String alias, String userIdColumn, Set<Long> orgIds) {
        String prefix = alias != null && !alias.isEmpty() ? alias + "." : "";

        if (orgIds == null) {
            // ALL — 不限制 / No restriction
            return "";
        }

        if (orgIds.isEmpty()) {
            // SELF — 仅本人数据 / Self data only
            // 使用占位符 ${currentUserId}，实际执行时由调用方替换 / Use placeholder, replace at execution time
            return " AND (" + prefix + userIdColumn + " = #{currentUserId})";
        }

        // 按组织节点过滤 / Filter by org node IDs
        String orgIdList = orgIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        return " AND (" + prefix + "org_id IN (" + orgIdList + "))";
    }

    /**
     * 构造带当前用户ID的过滤条件（SELF 场景精确版本） /
     * Build filter condition with explicit current user ID (precise version for SELF scenario)
     *
     * @param alias         表别名 / Table alias
     * @param userIdColumn  用户ID字段名 / User ID column name
     * @param orgIds        可见组织节点ID集合 / Set of visible org node IDs
     * @param currentUserId 当前用户ID / Current user ID
     * @return SQL 条件片段 / SQL condition fragment
     */
    public static String buildScopeCondition(String alias, String userIdColumn,
                                             Set<Long> orgIds, Long currentUserId) {
        String prefix = alias != null && !alias.isEmpty() ? alias + "." : "";

        if (orgIds == null) {
            return "";
        }

        if (orgIds.isEmpty()) {
            return " AND (" + prefix + userIdColumn + " = " + currentUserId + ")";
        }

        String orgIdList = orgIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        return " AND (" + prefix + "org_id IN (" + orgIdList + "))";
    }

    /**
     * 判断是否为全部数据范围（ALL） / Check if it's full data scope (ALL)
     *
     * @param orgIds 可见组织节点ID集合 / Set of visible org node IDs
     * @return true 表示不限制数据 / true means no data restriction
     */
    public static boolean isAllScope(Set<Long> orgIds) {
        return orgIds == null;
    }

    /**
     * 判断是否为仅本人范围（SELF） / Check if it's self-only scope
     *
     * @param orgIds 可见组织节点ID集合 / Set of visible org node IDs
     * @return true 表示仅本人数据 / true means self data only
     */
    public static boolean isSelfScope(Set<Long> orgIds) {
        return orgIds != null && orgIds.isEmpty();
    }
}
