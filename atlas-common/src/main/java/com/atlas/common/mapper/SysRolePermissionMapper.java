package com.atlas.common.mapper;

import com.atlas.common.entity.SysRolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色权限关联 Mapper / Role-Permission Relation Mapper
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {

    /**
     * 查询角色已分配的权限ID列表 / Query permission IDs assigned to a role
     *
     * @param roleId 角色ID / Role ID
     * @return 权限ID列表 / List of permission IDs
     */
    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量删除角色的权限关联 / Batch delete role-permission mappings
     *
     * @param roleId 角色ID / Role ID
     * @param permissionIds 要删除的权限ID列表 / Permission IDs to remove
     * @return 删除行数 / Deleted rows count
     */
    @Delete("<script>" +
            "DELETE FROM sys_role_permission WHERE role_id = #{roleId} " +
            "AND permission_id IN " +
            "<foreach collection='permissionIds' item='pid' open='(' separator=',' close=')'>#{pid}</foreach>" +
            "</script>")
    int deleteByRoleIdAndPermissionIds(@Param("roleId") Long roleId,
                                        @Param("permissionIds") List<Long> permissionIds);

    /**
     * 删除角色所有权限关联 / Delete all permission mappings for a role
     *
     * @param roleId 角色ID / Role ID
     * @return 删除行数 / Deleted rows count
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 按权限ID删除所有关联（级联删除权限时使用） / Delete all mappings by permission ID (cascade on permission deletion)
     *
     * @param permissionId 权限ID / Permission ID
     * @return 删除行数 / Deleted rows count
     */
    @Delete("DELETE FROM sys_role_permission WHERE permission_id = #{permissionId}")
    int deleteByPermissionId(@Param("permissionId") Long permissionId);
}
