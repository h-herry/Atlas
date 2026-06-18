package com.atlas.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统权限实体 / System Permission Entity
 * <p>
 * 权限分为三类：FUNCTION(功能权限) / DATA(数据权限) / MENU(菜单权限)
 * Permission types: FUNCTION / DATA / MENU
 * <p>
 * 支持树形结构，通过 parent_id 实现权限分组 / Tree structure via parent_id
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("sys_permission")
public class SysPermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 权限标识(如 message:view) / Permission Code */
    private String code;

    /** 权限名称 / Permission Name */
    private String name;

    /** 所属模块 / Module */
    private String module;

    /** 类型(FUNCTION功能/DATA数据/MENU菜单) / Type */
    private String type;

    /** 父权限ID(0表示顶级) / Parent Permission ID (0 = top level) */
    private Long parentId;

    /** 排序号 / Sort Order */
    private Integer sortOrder;

    /** 状态(1启用0禁用) / Status (1-enabled 0-disabled) */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
