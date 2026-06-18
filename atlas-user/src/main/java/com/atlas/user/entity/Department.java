package com.atlas.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门实体 / Department entity
 */
@Data
@TableName("department")
public class Department {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long parentId;
    private String deptPath;
    private String deptName;
    private Integer deptLevel;
    private Long managerId;
    private Integer sortOrder;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
