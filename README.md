# Atlas 供应链管理系统

> 基于 Spring Boot 3.2 + Spring Cloud 2023 的微服务供应链管理平台，覆盖供应商 SRM、物料管理、9种采购模式、合同管理、库存、收货质检及工作流全链路。

## 技术栈

| 层次 | 选型 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot + Spring Cloud | 3.2.5 / 2023.0.1 |
| 网关 | Spring Cloud Gateway | 2023.0.1 |
| 注册/配置 | Nacos | 2.3+ |
| ORM | MyBatis-Plus | 3.5.6 |
| 工作流 | Flowable | 7.0.1 |
| 消息队列 | RocketMQ | 5.1.4 |
| 分布式事务 | Seata（AT 模式预留） | 1.8.0 |
| 限流熔断 | Sentinel | 2023.0.1.0 |
| 链路追踪 | Zipkin | — |
| 分布式锁 | Redisson | 3.27.2 |
| 认证 | JWT（jjwt） | 0.12.5 |
| 文档 | Knife4j | 4.5.0 |
| 工具库 | Hutool | 5.8.27 |

## 模块架构

```
atlas
├── atlas-common       公共基础设施（JWT/Redis/异常/锁/MQ/限流/链路追踪/审计日志）
├── atlas-gateway      API 网关（路由转发/CORS/Sentinel 网关限流）
├── atlas-user         用户认证、RBAC 权限管理
├── atlas-supplier     供应商 SRM（准入/绩效/协同/风险）+ 物料管理
├── atlas-contract     合同状态机流转管理
├── atlas-purchase     9种采购模式 + 乐观锁库存扣减
├── atlas-inventory    库存管理 + 乐观锁 + 流水追溯
├── atlas-receipt      收货质检 + 消息表补偿 + 入库通知
├── atlas-workflow     Flowable 7.0 审批工作流
├── atlas-open         开放平台（RESTful API / Webhook / 鉴权 / 监控）
└── sql                集中管理数据库脚本（V1~V91，共 19 个）
```

### 模块详细说明

| 模块 | 端口 | 数据库 | 核心职责 |
|------|------|--------|---------|
| atlas-common | - | - | 公共基础设施（30 个类） |
| atlas-gateway | 8080 | - | 7 条路由规则 + CORS + Nacos 服务发现 |
| atlas-user | 8081 | atlas_user | 用户认证、RBAC 权限（JWT + Redis） |
| atlas-supplier | 8082 | atlas_supplier | SRM 四大子域 + 物料管理四大子模块 |
| atlas-contract | 8083 | atlas_contract | 合同状态机（10 状态 + allowTargets） |
| atlas-purchase | 8084 | atlas_purchase | 9 种采购方式 + 乐观锁库存扣减 |
| atlas-inventory | 8085 | atlas_inventory | 库存管理 + 乐观锁 + 变更流水 |
| atlas-receipt | 8086 | atlas_receipt | 收货质检 + 消息表补偿 |
| atlas-workflow | 8087 | atlas_flowable | Flowable 审批流（启动/审批/查询） |
| atlas-open | 8090 | atlas_open | 开放平台（API 鉴权/Webhook/调用日志） |

### 供应商模块 — SRM + 物料管理

**SRM 四大功能域**：准入管理（招募→注册→三级审批→自动入库）、绩效评估（多维度打分→自动定级→整改闭环）、供需协同（预测→发货→对账→付款）、风险管理（五类风险+四类预警+黑名单拦截）

**物料管理四大子模块**：基础数据管理（主数据/分类/多单位）、需求与计划（BOM/MRP）、采购执行协同（发货/生产进度/IQC）、质量追溯与分析（批次追溯/绩效/分析报表）

### 采购模块 — 9 种采购模式

公开招标、邀请招标、竞争性谈判、竞争性磋商、询价、竞价/拍卖、单一来源、合作创新、框架协议采购

## 快速启动

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis 7.0+
- RocketMQ 5.1+
- Maven 3.8+

### 数据库初始化

```bash
# 执行 sql/ 目录下脚本，按 V1 ~ V91 顺序
for f in sql/V*.sql; do mysql -u root -p < $f; done
```

### 编译运行

```bash
mvn clean install -DskipTests
# 按需启动各模块，或通过 gateway 统一访问
cd atlas-gateway && mvn spring-boot:run
cd atlas-user && mvn spring-boot:run
cd atlas-supplier && mvn spring-boot:run
# ...
```

## 接口文档

各模块启动后访问 Knife4j：
- atlas-gateway: http://localhost:8080/doc.html
- atlas-user: http://localhost:8081/doc.html
- atlas-supplier: http://localhost:8082/doc.html
- atlas-purchase: http://localhost:8084/doc.html

## 安全模型

- 无状态 JWT（HMAC-SHA256） + Redis 权限缓存（2h TTL）
- `@RequirePermission` 声明式权限控制
- Token 黑名单登出失效机制
- 登录失败锁定（5 次失败锁定 15 分钟）
- 统一 `BizException(ErrorCode)` 业务异常
- `@RateLimit` 接口限流（Guava RateLimiter 本地兜底）
- Sentinel 限流熔断（7 模块 + gateway，双数据源纳管到 Nacos）

## 项目结构规范

```
src/main/java/com/atlas/{module}/
├── controller/    Controller（@RequirePermission + Result 统一响应）
├── service/       Service（@Transactional + 状态机 + BizException）
├── mapper/        Mapper（BaseMapper + 自定义 SQL）
├── entity/        Entity（@TableName + ASSIGN_ID）
├── enums/         枚举（状态机/类型）
├── dto/           DTO（请求/响应）
├── model/         模型类
└── config/        模块配置
```

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0.0 | 2026-03 | 基础 CRUD 模块搭建 |
| 1.0.1 | 2026-06-17 | 状态机推广/Redis缓存/链路追踪/接口限流/商品主数据等优化 |
| 1.2.10 | 2026-06-17 | SRM 对标某行业头部四域升级：供应商配额/整改/财务结算/预测协同/竞价大厅/价格库/合同签章/风险预警/供应商推荐/开放平台 |
| 1.2.21 | 2026-06-18 | 供应商门户(双通道准入+竞价大厅+订单协同)、消息中心(WebSocket+邮件+短信)、电子合同管理(模板+签署+条款比对+履约)、基础设施全面升级 |
