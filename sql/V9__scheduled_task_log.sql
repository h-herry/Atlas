-- 定时任务执行日志表
CREATE TABLE scheduled_task_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_ms BIGINT COMMENT '执行耗时(毫秒)',
    status VARCHAR(16) COMMENT '执行状态: SUCCESS/FAILED',
    error_msg TEXT COMMENT '错误信息',
    INDEX idx_task_time (task_name, start_time)
) COMMENT '定时任务执行日志';
