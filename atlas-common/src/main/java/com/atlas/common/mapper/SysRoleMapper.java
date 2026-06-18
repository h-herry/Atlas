package com.atlas.common.mapper;

import com.atlas.common.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统角色 Mapper / System Role Mapper
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询用户所有角色列表 / Query all roles for a user
     * <p>
     * 通过用户角色关联表获取角色信息，包含 org_node_id 用于数据隔离 /
     * Retrieves roles via user-role relation, includes org_node_id for data isolation
     *
     * @param userId 用户ID / User ID
     * @return 角色实体列表 / List of role entities
     */
    @Select("SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<SysRole> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户角色及关联的组织节点 / Query user roles with org node info
     *
     * @param userId 用户ID / User ID
     * @return 角色列表(含 org_node_id) / Role list (with org_node_id)
     */
    @Select("SELECT r.*, ur.org_node_id FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<SysRole> selectByUserIdWithOrg(@Param("userId") Long userId);

    /**
     * 根据编码查询角色 / Query role by code
     *
     * @param code 角色编码 / Role code
     * @return 角色实体 / Role entity
     */
    @Select("SELECT * FROM sys_role WHERE code = #{code} AND status = 1")
    SysRole selectByCode(@Param("code") String code);
}
