-- ============================================
-- Atlas V94: 多语言 i18n 国际化支持
-- 数据库驱动的多语言方案，支持动态配置所有翻译文本
-- ============================================

-- 语言表
CREATE TABLE IF NOT EXISTS i18n_language (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    code            VARCHAR(10) NOT NULL COMMENT '语言代码 zh-CN/en-US/ja-JP',
    name            VARCHAR(50) NOT NULL COMMENT '语言名称 简体中文/English/日本語',
    native_name     VARCHAR(50) COMMENT '本地名称 简体中文/English/日本語',
    enabled         TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
    sort_order      INT DEFAULT 0 COMMENT '排序',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='语言表';

-- 翻译消息表
CREATE TABLE IF NOT EXISTS i18n_message (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_key     VARCHAR(200) NOT NULL COMMENT '消息键',
    language_code   VARCHAR(10) NOT NULL COMMENT '语言代码',
    message_value   TEXT NOT NULL COMMENT '翻译文本',
    module          VARCHAR(64) COMMENT '所属模块',
    description     VARCHAR(200) COMMENT '描述',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_key_lang (message_key, language_code),
    INDEX idx_module (module),
    INDEX idx_lang (language_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='翻译消息表';

-- ============================================
-- 种子数据：初始语言
-- ============================================
INSERT INTO i18n_language (code, name, native_name, enabled, sort_order) VALUES
('zh-CN', '简体中文', '简体中文', 1, 1),
('en-US', 'English', 'English', 1, 2);

-- ============================================
-- 种子数据：通用消息 (common 模块, 中英文)
-- ============================================

-- ---------- 通用响应消息 ----------
INSERT INTO i18n_message (message_key, language_code, message_value, module, description) VALUES
('common.success', 'zh-CN', '操作成功', 'common', '通用成功消息'),
('common.success', 'en-US', 'Operation successful', 'common', 'Generic success message'),
('common.error', 'zh-CN', '系统繁忙，请稍后重试', 'common', '通用错误消息'),
('common.error', 'en-US', 'System is busy, please try again later', 'common', 'Generic error message'),
('common.unauthorized', 'zh-CN', '未登录或Token已过期', 'common', '未授权'),
('common.unauthorized', 'en-US', 'Not logged in or token has expired', 'common', 'Unauthorized'),
('common.forbidden', 'zh-CN', '权限不足', 'common', '无权限'),
('common.forbidden', 'en-US', 'Access denied', 'common', 'Forbidden'),
('common.not_found', 'zh-CN', '资源不存在', 'common', '资源未找到'),
('common.not_found', 'en-US', 'Resource not found', 'common', 'Resource not found'),
('common.param_invalid', 'zh-CN', '请求参数错误', 'common', '参数校验失败'),
('common.param_invalid', 'en-US', 'Invalid request parameters', 'common', 'Parameter validation failed'),
('common.param_missing', 'zh-CN', '缺少必要参数: {0}', 'common', '缺少参数'),
('common.param_missing', 'en-US', 'Missing required parameter: {0}', 'common', 'Missing parameter'),
('common.duplicate', 'zh-CN', '数据已存在，请勿重复提交', 'common', '重复提交'),
('common.duplicate', 'en-US', 'Data already exists, please do not submit again', 'common', 'Duplicate submission'),
('common.export_success', 'zh-CN', '导出成功', 'common', '导出成功'),
('common.export_success', 'en-US', 'Export successful', 'common', 'Export success'),
('common.import_success', 'zh-CN', '导入成功，共 {0} 条', 'common', '导入成功'),
('common.import_success', 'en-US', 'Import successful, total {0} records', 'common', 'Import success'),
('common.save_success', 'zh-CN', '保存成功', 'common', '保存成功'),
('common.save_success', 'en-US', 'Save successful', 'common', 'Save success'),
('common.update_success', 'zh-CN', '更新成功', 'common', '更新成功'),
('common.update_success', 'en-US', 'Update successful', 'common', 'Update success'),
('common.delete_success', 'zh-CN', '删除成功', 'common', '删除成功'),
('common.delete_success', 'en-US', 'Delete successful', 'common', 'Delete success'),
('common.submit_success', 'zh-CN', '提交成功', 'common', '提交成功'),
('common.submit_success', 'en-US', 'Submit successful', 'common', 'Submit success'),
('common.approve_success', 'zh-CN', '审批成功', 'common', '审批成功'),
('common.approve_success', 'en-US', 'Approval successful', 'common', 'Approve success'),
('common.reject_success', 'zh-CN', '驳回成功', 'common', '驳回成功'),
('common.reject_success', 'en-US', 'Rejection successful', 'common', 'Reject success'),
('common.refresh_success', 'zh-CN', '刷新成功', 'common', '刷新成功'),
('common.refresh_success', 'en-US', 'Refresh successful', 'common', 'Refresh success');

-- ---------- 用户模块消息 ----------
INSERT INTO i18n_message (message_key, language_code, message_value, module, description) VALUES
('user.not_exist', 'zh-CN', '用户不存在', 'user', '用户不存在'),
('user.not_exist', 'en-US', 'User does not exist', 'user', 'User not found'),
('user.disabled', 'zh-CN', '用户已被禁用', 'user', '用户禁用'),
('user.disabled', 'en-US', 'User has been disabled', 'user', 'User disabled'),
('user.username_duplicate', 'zh-CN', '用户名已存在', 'user', '用户名重复'),
('user.username_duplicate', 'en-US', 'Username already exists', 'user', 'Duplicate username'),
('user.password_error', 'zh-CN', '密码错误', 'user', '密码错误'),
('user.password_error', 'en-US', 'Incorrect password', 'user', 'Wrong password'),
('user.login_success', 'zh-CN', '登录成功', 'user', '登录成功'),
('user.login_success', 'en-US', 'Login successful', 'user', 'Login success'),
('user.logout_success', 'zh-CN', '登出成功', 'user', '登出成功'),
('user.logout_success', 'en-US', 'Logout successful', 'user', 'Logout success'),
('user.password_changed', 'zh-CN', '密码修改成功', 'user', '密码修改'),
('user.password_changed', 'en-US', 'Password changed successfully', 'user', 'Password changed'),
('user.role_not_exist', 'zh-CN', '角色不存在', 'user', '角色不存在'),
('user.role_not_exist', 'en-US', 'Role does not exist', 'user', 'Role not found');

-- ---------- 供应商模块消息 ----------
INSERT INTO i18n_message (message_key, language_code, message_value, module, description) VALUES
('supplier.not_exist', 'zh-CN', '供应商不存在', 'supplier', '供应商不存在'),
('supplier.not_exist', 'en-US', 'Supplier does not exist', 'supplier', 'Supplier not found'),
('supplier.frozen', 'zh-CN', '供应商已被冻结', 'supplier', '供应商冻结'),
('supplier.frozen', 'en-US', 'Supplier has been frozen', 'supplier', 'Supplier frozen'),
('supplier.qualification_expired', 'zh-CN', '供应商资质已过期', 'supplier', '资质过期'),
('supplier.qualification_expired', 'en-US', 'Supplier qualification has expired', 'supplier', 'Qualification expired'),
('supplier.register_success', 'zh-CN', '供应商注册成功', 'supplier', '注册成功'),
('supplier.register_success', 'en-US', 'Supplier registration successful', 'supplier', 'Registration success'),
('supplier.qualify_approved', 'zh-CN', '资质审批通过', 'supplier', '资质通过'),
('supplier.qualify_approved', 'en-US', 'Qualification approved', 'supplier', 'Qualification passed'),
('supplier.qualify_rejected', 'zh-CN', '资质审批不通过', 'supplier', '资质拒绝'),
('supplier.qualify_rejected', 'en-US', 'Qualification rejected', 'supplier', 'Qualification rejected'),
('supplier.evaluate_submitted', 'zh-CN', '绩效评估已提交', 'supplier', '评估提交'),
('supplier.evaluate_submitted', 'en-US', 'Performance evaluation submitted', 'supplier', 'Evaluation submitted'),
('supplier.rectification_submitted', 'zh-CN', '整改方案已提交', 'supplier', '整改提交'),
('supplier.rectification_submitted', 'en-US', 'Rectification plan submitted', 'supplier', 'Rectification submitted');

-- ---------- 合同模块消息 ----------
INSERT INTO i18n_message (message_key, language_code, message_value, module, description) VALUES
('contract.not_exist', 'zh-CN', '合同不存在', 'contract', '合同不存在'),
('contract.not_exist', 'en-US', 'Contract does not exist', 'contract', 'Contract not found'),
('contract.already_approved', 'zh-CN', '合同已审批，不可重复提交', 'contract', '重复审批'),
('contract.already_approved', 'en-US', 'Contract already approved, cannot resubmit', 'contract', 'Already approved'),
('contract.amount_exceed', 'zh-CN', '合同金额超出预算', 'contract', '超出预算'),
('contract.amount_exceed', 'en-US', 'Contract amount exceeds budget', 'contract', 'Budget exceeded'),
('contract.created', 'zh-CN', '合同创建成功', 'contract', '合同创建'),
('contract.created', 'en-US', 'Contract created successfully', 'contract', 'Contract created'),
('contract.expiring_soon', 'zh-CN', '合同即将到期', 'contract', '合同到期提醒'),
('contract.expiring_soon', 'en-US', 'Contract expiring soon', 'contract', 'Contract expiry warning'),
('contract.signed', 'zh-CN', '合同已签章', 'contract', '合同签章'),
('contract.signed', 'en-US', 'Contract signed', 'contract', 'Contract signed');

-- ---------- 采购模块消息 ----------
INSERT INTO i18n_message (message_key, language_code, message_value, module, description) VALUES
('purchase.order_not_exist', 'zh-CN', '采购订单不存在', 'purchase', '订单不存在'),
('purchase.order_not_exist', 'en-US', 'Purchase order does not exist', 'purchase', 'Order not found'),
('purchase.order_cannot_modify', 'zh-CN', '当前状态不允许修改订单', 'purchase', '订单不可修改'),
('purchase.order_cannot_modify', 'en-US', 'Order cannot be modified in current status', 'purchase', 'Cannot modify'),
('purchase.order_duplicate', 'zh-CN', '重复提交（幂等校验未通过）', 'purchase', '重复提交'),
('purchase.order_duplicate', 'en-US', 'Duplicate submission (idempotent check failed)', 'purchase', 'Duplicate order'),
('purchase.order_submitted', 'zh-CN', '采购订单已提交', 'purchase', '订单提交'),
('purchase.order_submitted', 'en-US', 'Purchase order submitted', 'purchase', 'Order submitted'),
('purchase.order_approved', 'zh-CN', '采购订单已审批', 'purchase', '订单审批'),
('purchase.order_approved', 'en-US', 'Purchase order approved', 'purchase', 'Order approved'),
('purchase.bid_started', 'zh-CN', '竞价已启动', 'purchase', '竞价启动'),
('purchase.bid_started', 'en-US', 'Bidding started', 'purchase', 'Bid started'),
('purchase.bid_closed', 'zh-CN', '竞价已结束', 'purchase', '竞价结束'),
('purchase.bid_closed', 'en-US', 'Bidding closed', 'purchase', 'Bid closed');

-- ---------- 库存模块消息 ----------
INSERT INTO i18n_message (message_key, language_code, message_value, module, description) VALUES
('inventory.stock_insufficient', 'zh-CN', '库存不足', 'inventory', '库存不足'),
('inventory.stock_insufficient', 'en-US', 'Insufficient stock', 'inventory', 'Stock low'),
('inventory.sku_not_exist', 'zh-CN', 'SKU不存在', 'inventory', 'SKU不存在'),
('inventory.sku_not_exist', 'en-US', 'SKU does not exist', 'inventory', 'SKU not found'),
('inventory.stock_in', 'zh-CN', '入库成功', 'inventory', '入库'),
('inventory.stock_in', 'en-US', 'Stock-in successful', 'inventory', 'Stock in'),
('inventory.stock_out', 'zh-CN', '出库成功', 'inventory', '出库'),
('inventory.stock_out', 'en-US', 'Stock-out successful', 'inventory', 'Stock out'),
('inventory.transfer_success', 'zh-CN', '调拨成功', 'inventory', '调拨'),
('inventory.transfer_success', 'en-US', 'Transfer successful', 'inventory', 'Transfer');

-- ---------- 收货模块消息 ----------
INSERT INTO i18n_message (message_key, language_code, message_value, module, description) VALUES
('receipt.not_exist', 'zh-CN', '收货单不存在', 'receipt', '收货单不存在'),
('receipt.not_exist', 'en-US', 'Receipt does not exist', 'receipt', 'Receipt not found'),
('receipt.duplicate', 'zh-CN', '收货单已存在', 'receipt', '重复收货'),
('receipt.duplicate', 'en-US', 'Receipt already exists', 'receipt', 'Duplicate receipt'),
('receipt.confirmed', 'zh-CN', '收货确认成功', 'receipt', '收货确认'),
('receipt.confirmed', 'en-US', 'Receipt confirmed successfully', 'receipt', 'Receipt confirmed'),
('receipt.inspected', 'zh-CN', '检验完成', 'receipt', '检验完成'),
('receipt.inspected', 'en-US', 'Inspection completed', 'receipt', 'Inspection done'),
('receipt.quality_reject', 'zh-CN', '质量不合格，已退回', 'receipt', '质检不合格'),
('receipt.quality_reject', 'en-US', 'Quality rejected, returned', 'receipt', 'Quality failed');

-- ---------- 工作流模块消息 ----------
INSERT INTO i18n_message (message_key, language_code, message_value, module, description) VALUES
('workflow.not_exist', 'zh-CN', '工作流实例不存在', 'workflow', '工作流不存在'),
('workflow.not_exist', 'en-US', 'Workflow instance does not exist', 'workflow', 'Workflow not found'),
('workflow.task_not_found', 'zh-CN', '待审批任务不存在', 'workflow', '任务不存在'),
('workflow.task_not_found', 'en-US', 'Approval task not found', 'workflow', 'Task not found'),
('workflow.task_completed', 'zh-CN', '任务已处理', 'workflow', '任务完成'),
('workflow.task_completed', 'en-US', 'Task completed', 'workflow', 'Task done'),
('workflow.process_started', 'zh-CN', '流程已启动', 'workflow', '流程启动'),
('workflow.process_started', 'en-US', 'Process started', 'workflow', 'Process started'),
('workflow.process_ended', 'zh-CN', '流程已结束', 'workflow', '流程结束'),
('workflow.process_ended', 'en-US', 'Process ended', 'workflow', 'Process ended'),
('workflow.process_recalled', 'zh-CN', '流程已撤回', 'workflow', '流程撤回'),
('workflow.process_recalled', 'en-US', 'Process recalled', 'workflow', 'Process recalled');

-- ---------- 开放平台消息 ----------
INSERT INTO i18n_message (message_key, language_code, message_value, module, description) VALUES
('open.auth_failed', 'zh-CN', '接口鉴权失败', 'open', '鉴权失败'),
('open.auth_failed', 'en-US', 'API authentication failed', 'open', 'Auth failed'),
('open.rate_limited', 'zh-CN', '接口调用频率超限', 'open', '频率限制'),
('open.rate_limited', 'en-US', 'API rate limit exceeded', 'open', 'Rate limited'),
('open.config_updated', 'zh-CN', '接口配置已更新', 'open', '配置更新'),
('open.config_updated', 'en-US', 'API configuration updated', 'open', 'Config updated'),
('open.connectivity_ok', 'zh-CN', '连通性测试通过', 'open', '连通性OK'),
('open.connectivity_ok', 'en-US', 'Connectivity test passed', 'open', 'Connectivity OK'),
('open.connectivity_fail', 'zh-CN', '连通性测试失败: {0}', 'open', '连通性失败'),
('open.connectivity_fail', 'en-US', 'Connectivity test failed: {0}', 'open', 'Connectivity fail');
