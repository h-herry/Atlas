package com.atlas.common.mapper;

import com.atlas.common.entity.SysPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统权限 Mapper / System Permission Mapper
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 查询用户所有权限列表 / Query all permission codes for a user
     * <p>
     * 通过用户角色关联表联查，返回去重的权限标识码 /
     * Joins through user-role and role-permission tables, returns distinct permission codes
     *
     * @param userId 用户ID / User ID
     * @return 权限标识码列表 / List of permission codes
     */
    @Select("SELECT DISTINCT p.code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "INNER JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND p.status = 1 AND r.status = 1")
    List<String> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户所有权限详情列表 / Query all permission details for a user
     *
     * @param userId 用户ID / User ID
     * @return 权限实体列表 / List of permission entities
     */
    @Select("SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "INNER JOIN sys_role r ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND p.status = 1 AND r.status = 1 " +
            "ORDER BY p.sort_order ASC")
    List<SysPermission> selectDetailByUserId(@Param("userId") Long userId);

    /**
     * 按模块查询权限列表 / Query permissions by module
     *
     * @param module 模块名称 / Module name
     * @return 权限列表 / Permission list
     */
    @Select("SELECT * FROM sys_permission WHERE module = #{module} AND status = 1 ORDER BY sort_order ASC")
    List<SysPermission> selectByModule(@Param("module") String module);
}
