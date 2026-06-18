# ShardingSphere 分库分表策略 / ShardingSphere Database & Table Sharding Strategy

> 版本 / Version: atlas 1.2.10 | 组件 / Component: ShardingSphere-JDBC 5.4.1

## 分片策略总览 / Sharding Strategy Overview

| 表 / Table | 分库键 / DB Shard Key | 分表键 / Table Shard Key | 分片数 / Shard Count | 算法 / Algorithm | 所属模块 / Module |
|----|--------|--------|--------|------|---------|
| purchase_order | id % 2 | id % 4 | 2库×4表 / 2 DBs × 4 Tables | INLINE | atlas-purchase |
| audit_log | month % 2 | month % 4 | 2库×4表 / 2 DBs × 4 Tables | INLINE | atlas-purchase |
| api_integration_log | config_id % 2 | config_id % 4 | 2库×4表 / 2 DBs × 4 Tables | INLINE | atlas-open |

## 数据节点详情 / Data Node Details

### purchase_order
- 逻辑表 / Logical table: `purchase_order`
- 物理表 / Physical tables:
  - `atlas_purchase_0.purchase_order_0` ~ `atlas_purchase_0.purchase_order_3`
  - `atlas_purchase_1.purchase_order_0` ~ `atlas_purchase_1.purchase_order_3`
- 分库 / DB sharding: `ds$->{id % 2}`
- 分表 / Table sharding: `purchase_order_$->{id % 4}`

### audit_log
- 逻辑表 / Logical table: `audit_log`
- 物理表 / Physical tables: `ds$->{0..1}.audit_log_$->{0..3}`
- 分库 / DB sharding: `ds$->{java.time.YearMonth.from(created_at).getMonthValue() % 2}` (按月份 / by month)
- 分表 / Table sharding: `audit_log_$->{java.time.YearMonth.from(created_at).getMonthValue() % 4}` (按月份 / by month)

### api_integration_log
- 逻辑表 / Logical table: `api_integration_log`
- 物理表 / Physical tables: `ds$->{0..1}.api_integration_log_$->{0..3}`
- 分库 / DB sharding: `ds$->{config_id % 2}`
- 分表 / Table sharding: `api_integration_log_$->{config_id % 4}`

## 注意事项 / Cautions

1. ShardingSphere 会接管 DataSource，需要排除 Spring Boot 默认 DataSource 自动配置 / ShardingSphere takes over DataSource; exclude Spring Boot default DataSource auto-configuration
2. 分片键字段禁止更新（会破坏数据路由一致性） / Shard key fields must not be updated (would break routing consistency)
3. 跨库 JOIN / 子查询受限，优先使用 ShardingSphere 的 binding-tables 或应用层聚合 / Cross-database JOINs and subqueries are limited; prefer binding-tables or application-layer aggregation
4. audit_log 按 created_at 月份分片，跨月查询会路由到多库多表 / audit_log is sharded by month; cross-month queries route to multiple databases and tables
