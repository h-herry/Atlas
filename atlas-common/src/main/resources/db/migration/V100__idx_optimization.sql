-- =============================================================================
-- V100__idx_optimization.sql
-- 核心表复合索引补充 / Core Table Composite Index Optimization
-- 日期: 2026-06-18 / Date: 2026-06-18
-- =============================================================================

-- 供应商注册表索引：按供应商ID+状态联合查询加速 / Supplier Registration Index: accelerate supplier_id + status queries
CREATE INDEX IF NOT EXISTS idx_supplier_status ON sup_portal_register(supplier_id, status);

-- 供应商订单表索引：按供应商+状态+时间排序优化 / Supplier Order Index: optimize supplier_id + status + create_time ordering
CREATE INDEX IF NOT EXISTS idx_supplier_status_time ON sup_portal_order(supplier_id, status, create_time);

-- 消息记录表索引：按用户+已读状态+时间倒序查询 / Message Record Index: user_id + is_read + create_time DESC queries
CREATE INDEX IF NOT EXISTS idx_user_read_time ON msg_record(user_id, is_read, create_time);

-- 合同签署流索引：按合同ID+签署状态关联查询 / Contract Sign Flow Index: contract_id + status join queries
CREATE INDEX IF NOT EXISTS idx_contract_status ON cnt_sign_flow(contract_id, status);

-- 询价报价表索引：按询价单+供应商维度去重查询 / Inquiry Quotation Index: inquiry_id + supplier_id dedup queries
CREATE INDEX IF NOT EXISTS idx_inquiry_supplier ON inquiry_quotation(inquiry_id, supplier_id);
