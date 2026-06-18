-- ============================================================
-- V96: 消息推送基础设施 — 消息记录表 + 消息模板表 /
--      Message push infrastructure — message record + template tables
-- ============================================================

-- 消息记录表 / Message record table
CREATE TABLE IF NOT EXISTS msg_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID / Primary key',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID / Supplier ID',
    user_id BIGINT COMMENT '接收用户ID（内部用户）/ Recipient user ID (internal user)',
    title VARCHAR(200) COMMENT '消息标题 / Message title',
    content TEXT COMMENT '消息内容 / Message content',
    type VARCHAR(50) NOT NULL COMMENT '消息类型: ORDER/DELIVERY/SETTLEMENT/SYSTEM/APPROVAL/QUALITY / Message type',
    related_id VARCHAR(100) COMMENT '关联业务ID（如订单号）/ Related business ID (e.g. order number)',
    related_type VARCHAR(50) COMMENT '关联业务类型 / Related business type',
    channel VARCHAR(20) DEFAULT 'WEBSOCKET' COMMENT '推送渠道: WEBSOCKET/EMAIL/SMS / Push channel',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读 / Is read: 0=unread 1=read',
    read_at DATETIME COMMENT '已读时间 / Read timestamp',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Creation timestamp',
    INDEX idx_supplier_unread (supplier_id, is_read, created_at),
    INDEX idx_type (type, created_at),
    INDEX idx_user (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息记录表 / Message record table';

-- 消息模板表 / Message template table
CREATE TABLE IF NOT EXISTS msg_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID / Primary key',
    template_code VARCHAR(50) UNIQUE NOT NULL COMMENT '模板编码（唯一）/ Template code (unique)',
    title_template VARCHAR(300) COMMENT '标题模板（支持占位符 {supplierName} {orderNo} 等）/ Title template (supports placeholders)',
    content_template TEXT COMMENT '内容模板 / Content template',
    type VARCHAR(50) COMMENT '消息类型 / Message type',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用: 1启用 0禁用 / Is active: 1=enabled 0=disabled',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Creation timestamp'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板表 / Message template table';

-- ============================================================
-- 预置消息模板 / Predefined message templates
-- ============================================================

-- 1. 新订单通知 / New Order Notification
INSERT INTO msg_template (template_code, title_template, content_template, type, is_active) VALUES
('ORDER_CREATED',
 '新订单通知 / New Order Notification',
 '您收到一笔新订单: {orderNo}，金额 ￥{amount}，请及时确认并安排生产。 / You have received a new order: {orderNo}, amount ¥{amount}, please confirm and arrange production promptly.',
 'ORDER', 1);

-- 2. 订单状态变更 / Order Status Changed
INSERT INTO msg_template (template_code, title_template, content_template, type, is_active) VALUES
('ORDER_STATUS_CHANGED',
 '订单状态变更 / Order Status Changed',
 '订单 {orderNo} 状态已变更为【{statusName}】。如有疑问请联系采购方。 / Order {orderNo} status has changed to [{statusName}]. Contact the purchaser if you have any questions.',
 'ORDER', 1);

-- 3. 发货通知 / Shipment Notification
INSERT INTO msg_template (template_code, title_template, content_template, type, is_active) VALUES
('DELIVERY_SHIPPED',
 '发货通知 / Shipment Notification',
 '订单 {orderNo} 已发货，物流单号: {trackingNo}，预计 {estimatedArrival} 到达。 / Order {orderNo} has been shipped, tracking number: {trackingNo}, estimated arrival: {estimatedArrival}.',
 'DELIVERY', 1);

-- 4. 交付延迟预警 / Delivery Delay Alert
INSERT INTO msg_template (template_code, title_template, content_template, type, is_active) VALUES
('DELIVERY_DELAYED',
 '交付延迟预警 / Delivery Delay Alert',
 '订单 {orderNo} 交付已延迟！原定交期 {dueDate}，当前状态: {statusName}。请立即采取措施并与采购方沟通。 / Order {orderNo} delivery is delayed! Original due date: {dueDate}, current status: {statusName}. Take immediate action and communicate with the purchaser.',
 'DELIVERY', 1);

-- 5. 到货确认 / Goods Received Confirmation
INSERT INTO msg_template (template_code, title_template, content_template, type, is_active) VALUES
('DELIVERY_RECEIVED',
 '到货确认 / Goods Received Confirmation',
 '订单 {orderNo} 货物已于 {receivedDate} 确认收货。入库单号: {receiptNo}。 / Order {orderNo} goods received on {receivedDate}. Receipt number: {receiptNo}.',
 'DELIVERY', 1);

-- 6. 对账单生成 / Statement Generated
INSERT INTO msg_template (template_code, title_template, content_template, type, is_active) VALUES
('SETTLEMENT_STATEMENT',
 '对账单生成 / Statement Generated',
 '对账单 {billNo} 已生成，账单期间: {periodStart} 至 {periodEnd}，金额 ￥{amount}。请登录供应商门户确认。 / Statement {billNo} has been generated, billing period: {periodStart} to {periodEnd}, amount ¥{amount}. Please log in to supplier portal to confirm.',
 'SETTLEMENT', 1);

-- 7. 待审批通知 / Pending Approval
INSERT INTO msg_template (template_code, title_template, content_template, type, is_active) VALUES
('APPROVAL_PENDING',
 '待审批通知 / Pending Approval',
 '您有一项待审批: {approvalType} - {approvalTitle}。发起人: {initiator}，请及时处理。 / You have a pending approval: {approvalType} - {approvalTitle}. Initiator: {initiator}, please process promptly.',
 'APPROVAL', 1);

-- 8. 质检不合格通知 / Quality Inspection Failed
INSERT INTO msg_template (template_code, title_template, content_template, type, is_active) VALUES
('QUALITY_INSPECTION_FAIL',
 '质检不合格通知 / Quality Inspection Failed',
 '订单 {orderNo} 中物料 {materialName} 质检不合格。不合格项: {defectDesc}，处理意见: {action}。 / Material {materialName} in order {orderNo} failed quality inspection. Defect: {defectDesc}, action: {action}.',
 'QUALITY', 1);

-- 付款状态变更（补充模板，结算模块使用）/ Payment status changed (supplementary template for settlement module)
INSERT INTO msg_template (template_code, title_template, content_template, type, is_active) VALUES
('SETTLEMENT_PAYMENT_UPDATED',
 '付款状态变更 / Payment Status Updated',
 '对账单 {billNo} 付款状态已更新为【{statusName}】，金额 ￥{amount}。 / Statement {billNo} payment status updated to [{statusName}], amount ¥{amount}.',
 'SETTLEMENT', 1);
