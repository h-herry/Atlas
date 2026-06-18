package com.atlas.common.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 审计日志实体 / Audit log entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
public class AuditLog {

    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String operation;
    private String description;
    private String requestUri;
    private String requestMethod;
    private String requestParams;
    private String responseStatus;
    private String ipAddress;
    private Long durationMs;
    private LocalDateTime createdAt;
}
