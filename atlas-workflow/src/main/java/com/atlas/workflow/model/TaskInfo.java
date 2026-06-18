package com.atlas.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 工作流任务信息 DTO / Workflow task info DTO
 * <p>
 * 封装 Flowable Task 的核心字段，用于前端展示待办列表和审批历史。 /
 * Encapsulates Flowable Task core fields for pending list and approval history display.
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskInfo implements Serializable {

    /** 任务ID / Task ID */
    private String taskId;

    /** 任务名称 / Task name */
    private String taskName;

    /** 流程实例ID / Process instance ID */
    private String processInstanceId;

    /** 业务单据标识（businessKey） / Business document identifier (businessKey) */
    private String businessKey;

    /** 当前处理人 / Current assignee */
    private String assignee;

    /** 任务创建时间 / Task creation time */
    private LocalDateTime createTime;

    /** 流程变量（审批意见等） / Process variables (approval comments etc.) */
    private Map<String, Object> processVariables;
}
