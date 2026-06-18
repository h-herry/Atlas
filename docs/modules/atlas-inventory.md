# atlas-inventory — 库存管理 / Inventory Management

## 功能概述 / Overview

`atlas-inventory` 模块负责库存的核心操作：入库、出库、低库存预警、库存变更流水追踪。采用乐观锁（version 字段）保证并发安全，预留 Seata AT 模式分布式事务支持。

---

The `atlas-inventory` module handles core inventory operations: stock-in, stock-out, low-stock alerts, and inventory change log tracking. It uses optimistic locking (version field) for concurrency safety and reserves Seata AT mode distributed transaction support.

**端口 / Port**：8085 | **数据库 / Database**：atlas_inventory | **文件数 / File count**：9 Java classes

---

## 数据库表列表 / Database Tables（3 张 / 3 tables）

| 表名 / Table | 说明 / Description | 迁移 / Migration |
|------|------|------|
| inventory | 库存表（乐观锁 version 字段 + 安全库存阈值） / Inventory table (optimistic lock version + safety stock threshold) | V5 |
| inventory_log | 库存变更流水表（before/after 快照） / Inventory change log (before/after snapshot) | V5 |
| undo_log | Seata AT 模式回滚日志（预留） / Seata AT mode rollback log (reserved) | V5 |

---

## Controller API 列表 / Controller API List

### InventoryController（/api/inventory）

| 端点 / Endpoint | 方法 / Method | 权限 / Permission | 说明 / Description |
|------|------|------|------|
| `/api/inventory/{skuId}/deduct` | PUT | inventory:manage | 扣减库存（乐观锁 + 自旋重试） / Deduct stock (optimistic lock + spin retry) |
| `/api/inventory/add` | POST | inventory:manage | 入库增加库存 / Add stock |
| `/api/inventory/add/by-order/{orderNo}` | POST | inventory:manage | 按订单号关联入库 / Add stock by order number |
| `/api/inventory/page` | GET | inventory:view | 分页查询库存列表 / Paginated inventory query |
| `/api/inventory/low-stock` | GET | inventory:view | 低库存预警（低于安全阈值） / Low-stock alert (below safety threshold) |

---

## 核心 Service / Core Service

### InventoryService

- `deduct(skuId, quantity)`：乐观锁扣减，失败自动重试 3 次 / Optimistic lock deduction, auto-retry up to 3 times
- `addStock(skuId, quantity)`：增加库存 / Increase stock
- `addStockByOrderNo(orderNo, quantity)`：按订单号关联入库 / Add stock linked to order number
- `listLowStock()`：查询库存量 ≤ 安全库存阈值的记录 / Query records where stock ≤ safety threshold

---

## 技术要点 / Technical Details

### 乐观锁并发控制 / Optimistic Lock Concurrency Control

`inventory` 表 `version` 字段（数据库级别），MyBatis-Plus 自动支持 / Database-level version field, auto-supported by MyBatis-Plus：

```sql
UPDATE inventory SET stock = stock - ?, version = version + 1
WHERE id = ? AND version = ?
```

- Service 层自旋重试逻辑：失败后循环最多 3 次，每次间隔 50ms / Spin retry: max 3 attempts, 50ms interval
- 3 次全部失败 → 抛出 `BizException(STOCK_INSUFFICIENT)` / All 3 failed → throw BizException

### 库存变更流水 / Inventory Change Log

`inventory_log` 表每次操作记录 / Records per operation：
- `before_stock` / `after_stock`：变更前后库存量 / Stock before/after change
- `change_type`：DEDUCT（扣减）/ ADD（入库）/ Deduction / Addition
- `order_no`：关联订单号 / Linked order number
- `operator` / `operate_time`：操作人与时间 / Operator and timestamp

### 低库存预警 / Low-Stock Alert

- 查询条件 / Query condition：`stock <= safety_stock`
- 返回低库存的 SKU 列表（含当前库存量、安全阈值、缺口数量） / Returns SKU list with current stock, safety threshold, gap

### 分布式事务预留 / Distributed Transaction Reservation

`undo_log` 表用于 Seata AT 模式，当前为预留字段。未来如果采购-库存-收货需要强一致性，可启用 Seata AT 事务。 / The `undo_log` table is reserved for Seata AT mode. If purchase-inventory-receipt requires strong consistency in the future, Seata AT transactions can be enabled.
