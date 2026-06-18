-- 审计日志表
CREATE TABLE audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(64) COMMENT '操作用户名',
    module VARCHAR(32) COMMENT '模块: USER/SUPPLIER/CONTRACT/PURCHASE/INVENTORY/RECEIPT/WORKFLOW',
    operation VARCHAR(32) COMMENT '操作类型: CREATE/UPDATE/DELETE/QUERY/LOGIN/APPROVE',
    description VARCHAR(200) COMMENT '操作描述',
    request_uri VARCHAR(200) COMMENT '请求URI',
    request_method VARCHAR(10) COMMENT '请求方法: GET/POST/PUT/DELETE',
    request_params TEXT COMMENT '请求参数(JSON)',
    response_status VARCHAR(16) COMMENT '响应状态: SUCCESS/FAILED',
    ip_address VARCHAR(45) COMMENT '客户端IP',
    duration_ms BIGINT COMMENT '执行耗时(毫秒)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_time (user_id, created_at),
    INDEX idx_module (module, created_at)
) COMMENT '审计日志';
