package com.atlas.open.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API 调用日志 / API call log
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("api_call_log")
public class ApiCallLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 客户端ID / Client ID */
    private Long clientId;

    /** 请求 API 路径 / Request API path */
    private String apiPath;

    /** 请求方法: GET/POST/PUT/DELETE / Request method: GET/POST/PUT/DELETE */
    private String method;

    /** 请求 IP / Request IP */
    private String requestIp;

    /** 响应 HTTP 状态码 / Response HTTP status code */
    private Integer responseStatus;

    /** 处理耗时（毫秒） / Processing duration (ms) */
    private Long durationMs;

    /** 请求时间 / Request time */
    private LocalDateTime createdAt;
}
