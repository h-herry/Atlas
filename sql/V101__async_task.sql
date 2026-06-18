-- ============================================
-- Atlas V101: 异步任务追踪表 / Async Task Tracking Table
-- 基于 09 报告 2.1.5，支持批量导入/导出/消息推送等异步任务的状态追踪 /
-- Based on 09 report 2.1.5, supports async task status tracking for batch import/export/message push
-- ============================================

CREATE TABLE IF NOT EXISTS async_task (
    task_id         VARCHAR(64) PRIMARY KEY COMMENT '任务ID（UUID） / Task ID (UUID)',
    task_type       VARCHAR(32) NOT NULL COMMENT '任务类型: IMPORT/EXPORT/PUSH/BATCH / Task type',
    task_name       VARCHAR(128) COMMENT '任务名称 / Task name',
    status          TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0排队 1执行中 2成功 3失败 / Status: 0 queued 1 running 2 success 3 failed',
    params          TEXT COMMENT '任务参数（JSON） / Task parameters (JSON)',
    result          TEXT COMMENT '执行结果（JSON，含进度/错误信息） / Execution result (JSON, contains progress/error)',
    progress        INT DEFAULT 0 COMMENT '进度百分比 0-100 / Progress percentage 0-100',
    error_msg       VARCHAR(2000) COMMENT '错误信息 / Error message',
    retry_count     INT DEFAULT 0 COMMENT '已重试次数 / Retry count',
    max_retry       INT DEFAULT 3 COMMENT '最大重试次数 / Max retry count',
    created_by      VARCHAR(64) COMMENT '创建人ID / Creator ID',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    started_at      DATETIME COMMENT '开始执行时间 / Start time',
    finished_at     DATETIME COMMENT '完成时间 / Finish time',
    INDEX idx_type_status (task_type, status),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异步任务追踪表 / Async task tracking table';
