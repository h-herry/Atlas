package com.atlas.open.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * API 接口定义 / API endpoint definition
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("api_endpoint_config")
public class ApiEndpointConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联配置ID / Associated config ID */
    private Long configId;

    /** 接口编码 / Endpoint code */
    private String endpointCode;

    /** 接口名称 / Endpoint name */
    private String endpointName;

    /** 请求方法: GET/POST/PUT/DELETE / Request method */
    private String method;

    /** 接口路径 / Endpoint path */
    private String path;

    /** 请求模板(JSON)，支持变量占位符 {{xxx}} / Request template (JSON), supports variable placeholders {{xxx}} */
    private String requestTemplate;

    /** 响应映射JSON / Response mapping JSON */
    private String responseMapping;

    /** 成功判断: $.code==200 / Success condition */
    private String successCondition;

    /** 接口描述 / Endpoint description */
    private String description;

    /** 状态: 1启用 0禁用 / Status: 1-enabled 0-disabled */
    private Integer status;
}
