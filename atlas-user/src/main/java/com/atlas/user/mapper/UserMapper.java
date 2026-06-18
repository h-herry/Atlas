package com.atlas.user.mapper;

import com.atlas.user.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /** 根据用户名查询用户 / Query user by username */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(String username);

    /** 查询用户拥有的角色编码列表 / Query role codes owned by user */
    @Select("SELECT r.role_code FROM role r " +
            "INNER JOIN user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.status = 1")
    List<String> selectRoleCodes(Long userId);

    /** 查询用户拥有的权限标识列表 / Query permission codes owned by user */
    @Select("SELECT DISTINCT p.perm_code FROM permission p " +
            "INNER JOIN role_permission rp ON p.id = rp.perm_id " +
            "INNER JOIN user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectPermissionCodes(Long userId);

    /** 为用户分配角色（全量替换） / Assign roles to user (full replacement) */
    @org.apache.ibatis.annotations.Delete("DELETE FROM user_role WHERE user_id = #{userId}")
    int deleteUserRoles(Long userId);

    @org.apache.ibatis.annotations.Insert("<script>" +
            "INSERT INTO user_role (user_id, role_id) VALUES " +
            "<foreach collection='roleIds' item='roleId' separator=','>" +
            "(#{userId}, #{roleId})" +
            "</foreach>" +
            "</script>")
    int insertUserRoles(@org.apache.ibatis.annotations.Param("userId") Long userId,
                        @org.apache.ibatis.annotations.Param("roleIds") List<Long> roleIds);

    /** 查询用户拥有的权限详情列表 / Query permission details owned by user */
    @Select("SELECT DISTINCT p.* FROM permission p " +
            "INNER JOIN role_permission rp ON p.id = rp.perm_id " +
            "INNER JOIN user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<com.atlas.user.entity.Permission> selectPermissions(Long userId);
}
