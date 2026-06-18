package com.atlas.common.mapper;

import com.atlas.common.entity.SysUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色关联 Mapper / User-Role Relation Mapper
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 查询用户在当前组织节点下的所有角色关联 / Query all role assignments for a user under a given org node
     *
     * @param userId 用户ID / User ID
     * @return 用户角色关联列表 / User-role mapping list
     */
    @Select("SELECT * FROM sys_user_role WHERE user_id = #{userId}")
    List<SysUserRole> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户在指定组织节点的角色关联 / Query user role by org node
     *
     * @param userId    用户ID / User ID
     * @param orgNodeId 组织节点ID / Org node ID
     * @return 用户角色关联列表 / User-role mapping list
     */
    @Select("SELECT * FROM sys_user_role WHERE user_id = #{userId} AND org_node_id = #{orgNodeId}")
    List<SysUserRole> selectByUserIdAndOrgNodeId(@Param("userId") Long userId,
                                                   @Param("orgNodeId") Long orgNodeId);

    /**
     * 删除用户在指定组织节点的所有角色 / Remove all roles for a user under a given org node
     *
     * @param userId    用户ID / User ID
     * @param orgNodeId 组织节点ID / Org node ID
     * @return 删除行数 / Deleted rows count
     */
    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId} AND org_node_id = #{orgNodeId}")
    int deleteByUserIdAndOrgNodeId(@Param("userId") Long userId,
                                    @Param("orgNodeId") Long orgNodeId);

    /**
     * 查询拥有指定角色的所有用户ID / Query all user IDs assigned a given role
     *
     * @param roleId 角色ID / Role ID
     * @return 用户ID列表 / List of user IDs
     */
    @Select("SELECT DISTINCT user_id FROM sys_user_role WHERE role_id = #{roleId}")
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);
}
