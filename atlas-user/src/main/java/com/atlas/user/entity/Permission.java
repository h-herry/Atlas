package com.atlas.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 权限实体 / Permission entity
 */
@Data
@TableName("permission")
public class Permission {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long parentId;
    private String permCode;
    private String permName;
    private Integer permType;
}
