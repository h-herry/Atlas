-- ============================================
-- Atlas V100: 数据库复合索引优化 / Composite Index Optimization
-- 基于 09 报告 2.1.2，为高频查询表补充复合索引 /
-- Based on 09 report 2.1.2, add composite indexes for high-frequency query tables
-- ============================================

-- 1. 供应商门户注册表：按供应商ID+状态查询 / sup_portal_register: query by supplier_id + status
CREATE INDEX IF NOT EXISTS idx_supplier_status ON sup_portal_register (supplier_id, status);

-- 2. 供应商门户订单表：按供应商ID+状态+创建时间排序 / sup_portal_order: query by supplier_id + status + create_time
CREATE INDEX IF NOT EXISTS idx_supplier_status_time ON sup_portal_order (supplier_id, status, create_time);

-- 3. 消息记录表：按用户ID+已读状态+创建时间排序 / msg_record: query by user_id + is_read + create_time
CREATE INDEX IF NOT EXISTS idx_user_read_time ON msg_record (user_id, is_read, create_time);

-- 4. 合同签署流程表：按合同ID+状态查询 / cnt_sign_flow: query by contract_id + status
CREATE INDEX IF NOT EXISTS idx_contract_status ON cnt_sign_flow (contract_id, status);

-- 5. 询价报价表：按询价单ID+供应商ID查询 / inquiry_quotation: query by inquiry_id + supplier_id
CREATE INDEX IF NOT EXISTS idx_inquiry_supplier ON inquiry_quotation (inquiry_id, supplier_id);
