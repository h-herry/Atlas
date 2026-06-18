package com.atlas.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统角色实体 / System Role Entity
 * <p>
 * data_scope 定义了角色的数据权限范围：ALL/GROUP/DIVISION/PLANT/DEPT/SELF /
 * data_scope defines the data visibility range for this role
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("sys_role")
public class SysRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码 / Role Code */
    private String code;

    /** 角色名称 / Role Name */
    private String name;

    /**
     * 数据权限范围 / Data Scope
     * <ul>
     *   <li>ALL      — 全部数据 / All data</li>
     *   <li>GROUP    — 集团级 / Group level</li>
     *   <li>DIVISION — 事业部级 / Division level</li>
     *   <li>PLANT    — 工厂级 / Plant level</li>
     *   <li>DEPT     — 部门级 / Department level</li>
     *   <li>SELF     — 仅本人数据 / Self data only</li>
     * </ul>
     */
    private String dataScope;

    /** 角色描述 / Description */
    private String description;

    /** 状态(1启用0禁用) / Status (1-enabled 0-disabled) */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
