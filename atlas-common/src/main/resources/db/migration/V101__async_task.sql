-- =============================================================================
-- V101__async_task.sql
-- 异步任务追踪表 / Async Task Tracking Table
-- 日期: 2026-06-18 / Date: 2026-06-18
-- =============================================================================

CREATE TABLE IF NOT EXISTS async_task (
    task_id      VARCHAR(64)  NOT NULL                                 COMMENT '任务ID / Task ID',
    task_type    VARCHAR(32)  NOT NULL                                 COMMENT '任务类型(EXPORT/IMPORT/NOTIFY/BATCH等) / Task type',
    status       VARCHAR(16)  NOT NULL DEFAULT 'PENDING'               COMMENT '任务状态(PENDING/RUNNING/SUCCESS/FAILED) / Task status',
    params       TEXT                                                   COMMENT '任务参数JSON / Task parameters JSON',
    result       TEXT                                                   COMMENT '执行结果JSON / Execution result JSON',
    error_msg    VARCHAR(1024)                                          COMMENT '错误信息 / Error message',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP       COMMENT '创建时间 / Create time',
    finish_time  DATETIME                                               COMMENT '完成时间 / Finish time',
    PRIMARY KEY (task_id),
    INDEX idx_type_status (task_type, status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='异步任务追踪 / Async Task Tracking';
