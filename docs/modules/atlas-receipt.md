# atlas-receipt — 收货管理 / Receipt Management

## 功能概述 / Overview

`atlas-receipt` 模块负责收货单创建、质检和确认入库流程。通过消息表补偿机制保证收货确认与库存入库的数据最终一致性。

---

The `atlas-receipt` module handles receipt creation, quality inspection, and confirmed stock-in workflows. It uses an outbox message compensation mechanism to ensure eventual consistency between receipt confirmation and inventory stock-in.

**端口 / Port**：8086 | **数据库 / Database**：atlas_receipt | **文件数 / File count**：10 Java classes

---

## 数据库表列表 / Database Tables（3 张 / 3 tables）

| 表名 / Table | 说明 / Description | 迁移 / Migration |
|------|------|------|
| receipt | 收货单主表 / Receipt master table | V6 |
| receipt_item | 收货明细表（合格/不合格数量） / Receipt items (qualified/defective quantities) | V6 |
| receipt_outbox | 消息补偿表（待补录） / Outbox message compensation table (pending) | 待补录 / TBD |

---

## Controller API 列表 / Controller API List

### ReceiptController（/api/receipt）

| 端点 / Endpoint | 方法 / Method | 权限 / Permission | 说明 / Description |
|------|------|------|------|
| `/api/receipt` | POST | receipt:manage | 创建收货单 / Create receipt |
| `/api/receipt/{id}/quality-check` | PUT | receipt:manage | 质检（合格/不合格判定） / Quality check (qualified/defective judgment) |
| `/api/receipt/{id}/confirm` | PUT | receipt:manage | 确认收货（消息表补偿入库） / Confirm receipt (outbox compensation stock-in) |
| `/api/receipt/page` | GET | receipt:view | 分页查询收货单 / Paginated receipt query |
| `/api/receipt/{id}/items` | GET | receipt:view | 查询收货明细 / Query receipt items |

---

## 核心 Service / Core Service

### ReceiptService

- `createReceipt(orderNo, items)`：根据采购订单生成收货单 / Generate receipt from purchase order
- `qualityCheck(receiptId, itemResults)`：逐明细项质检（合格/不合格数量） / Per-item quality check (qualified/defective quantities)
- `confirmReceipt(receiptId)`：确认收货 → 写入 outbox → 通知库存入库 / Confirm receipt → write outbox → notify inventory stock-in

---

## 技术要点 / Technical Details

### 消息表补偿 / Outbox Message Compensation

确认收货时写入 `receipt_outbox` 表（状态 = PENDING），然后 / On confirmation, write to outbox (status = PENDING), then：

1. **实时发送 / Real-time send**：确认收货后立即尝试发消息通知库存模块 / Attempt to send message immediately after confirmation
2. **定时补发 / Scheduled retry**：定时任务扫描 outbox 中 PENDING/FAILED 记录，重试发送 / Scheduled job scans PENDING/FAILED records and retries
3. **最终一致 / Eventual consistency**：依赖 RocketMQ 消息 + outbox 补发，保证库存最终一致 / Relies on RocketMQ + outbox retry for eventual consistency

### 质检流程 / Quality Check Process

- 每个 `receipt_item` 记录 `received_qty`（实收数）、`qualified_qty`（合格数）、`defective_qty`（不合格数） / Each item records received, qualified, and defective quantities
- 质检完成后更新质检结果和质检员 / Update inspection result and inspector after QC
- 仅合格数量用于后续入库计算 / Only qualified quantities used for subsequent stock-in calculation

### 与采购/库存联动 / Integration with Purchase & Inventory

```
采购模块提交订单 → 库存扣减 → 收货模块确认收货 → 库存入库（消息通知）
Purchase submits order → Inventory deduction → Receipt confirms → Inventory stock-in (message notification)
```
