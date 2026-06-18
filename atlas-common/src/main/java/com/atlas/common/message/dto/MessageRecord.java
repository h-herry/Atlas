package com.atlas.common.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息记录响应 DTO / Message record response DTO
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRecord {

    private Long id;
    private Long supplierId;
    private Long userId;
    private String title;
    private String content;
    private String type;
    private String relatedId;
    private String relatedType;
    private String channel;
    private Integer priority;
    private Integer isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
