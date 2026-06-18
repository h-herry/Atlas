package com.atlas.open.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API 集成调用日志 / API integration call log
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("api_integration_log")
public class ApiIntegrationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联配置ID / Associated config ID */
    private Long configId;

    /** 接口编码 / Endpoint code */
    private String endpointCode;

    /** 请求URL / Request URL */
    private String requestUrl;

    /** 请求体 / Request body */
    private String requestBody;

    /** 响应体 / Response body */
    private String responseBody;

    /** 响应状态码 / Response status code */
    private Integer responseStatus;

    /** 耗时（毫秒） / Duration (ms) */
    private Long durationMs;

    /** 是否成功: 1成功 0失败 / Success flag: 1-success 0-failure */
    private Integer success;

    /** 错误信息 / Error message */
    private String errorMsg;

    /** 创建时间 / Creation time */
    private LocalDateTime createdAt;
}
