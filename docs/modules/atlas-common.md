# atlas-common — 公共基础设施 / Common Infrastructure

## 功能概述 / Overview

`atlas-common` 是整个项目的"心脏模块"，为其余 8 个模块提供统一的公共基础设施，包括认证授权（JWT）、缓存（Redis）、消息队列（RocketMQ）、分布式锁（Redisson）、限流熔断（Sentinel/Guava）、链路追踪（Zipkin）、AOP 审计日志、统一响应、全局异常处理等。

---

`atlas-common` is the "heart module" of the entire project, providing unified common infrastructure for the other 8 modules, including authentication & authorization (JWT), caching (Redis), message queue (RocketMQ), distributed locks (Redisson), rate limiting & circuit breaking (Sentinel/Guava), distributed tracing (Zipkin), AOP audit logging, unified response, global exception handling, and more.

**文件数**：30 个 Java 类 / **File count**: 30 Java classes

---

## 包结构 / Package Structure

```
com.atlas.common
├── core/enums/          ErrorCode（70+ 错误码 / 70+ error codes）
├── core/exception/      BizException、UnauthorizedException
├── core/util/           JwtUtil、SnowflakeIdGenerator
├── security/config/     SecurityConfig
├── security/filter/     JwtAuthenticationFilter
├── security/service/    TokenService、RenewTokenHolder
├── security/annotation/ RequirePermission、DataScope、AuditLog、RateLimit
├── security/interceptor/DataScopeInterceptor
├── cache/config/        RedisConfig
├── cache/lock/          RedisDistributedLock
├── mq/producer/         AbstractMessageProducer
├── mq/consumer/         AbstractMessageConsumer
├── aspect/              AuditLogAspect、RateLimitAspect
├── entity/              AuditLog、Goods、GoodsCategory
├── service/             AuditLogService
├── config/              SentinelConfig
└── web/                 GlobalExceptionHandler、Result、PageResult
```

---

## 核心组件 / Core Components

### ErrorCode 错误码枚举 / Error Code Enumeration

9 位编码体系：`模块(2) + 分类(2) + 具体(3) + 预留(2)`，已覆盖 70+ 错误码：

---

9-digit coding scheme: `module(2) + category(2) + specific(3) + reserved(2)`, covering 70+ error codes:

| 编码范围 / Code Range | 模块 / Module | 数量 / Count |
|----------|------|------|
| 200/400~500 | 通用 / General | 6 |
| 1001~1007/429/12020~12021 | 用户/限流/熔断/审计 / User/Rate Limit/Circuit Breaker/Audit | 12 |
| 2001~2003 | 供应商 / Supplier | 3 |
| 3001~3003 | 合同 / Contract | 3 |
| 4001~4003 | 采购 / Purchase | 3 |
| 5001~5002 | 库存 / Inventory | 2 |
| 6001~6002 | 收货 / Receipt | 2 |
| 7001~7002 | 工作流 / Workflow | 2 |
| 8001~8004 | SRM 准入 / SRM Access | 4 |
| 9001~9003 | SRM 评估 / SRM Evaluation | 3 |
| 10001~10004 | SRM 协同 / SRM Collaboration | 4 |
| 11001~11004 | SRM 风险 / SRM Risk | 4 |
| 12001~12018 | 物料管理 / Material Management | 18 |

### JWT 认证链路 / JWT Authentication Chain

```
请求 → JwtAuthenticationFilter → TokenService 验证 → SecurityContext → @RequirePermission AOP
```

---

```
Request → JwtAuthenticationFilter → TokenService validation → SecurityContext → @RequirePermission AOP
```

- Access Token（2h）+ Refresh Token，HMAC-SHA256 签名 / HMAC-SHA256 signature
- Token 黑名单（Redis）实现登出失效 / Token blacklist (Redis) enables logout invalidation
- `RenewTokenHolder`（ThreadLocal）无感续期上下文传递 / Silent renewal context propagation via ThreadLocal

### Result / PageResult 统一响应 / Unified Response

```java
Result<T> { code, message, data }
PageResult<T> { total, pages, current, size, records }
```

### 全局异常处理 / Global Exception Handling（GlobalExceptionHandler）

- `BizException` → 返回对应 ErrorCode / Returns corresponding ErrorCode
- `FlowException`（Sentinel 限流 / rate limiting）→ `RATE_LIMIT_EXCEEDED(429)`
- `DegradeException`（Sentinel 熔断 / circuit breaking）→ `CIRCUIT_BREAKER_OPEN(12020)`
- 未知异常 / Unknown exception → `INTERNAL_ERROR(500)` + 日志堆栈 / stack trace logging

### Sentinel 限流熔断 / Sentinel Rate Limiting & Circuit Breaking（SentinelConfig）

- `@PostConstruct` 注册 / registers `WebCallbackManager.setUrlBlockHandler`
- 限流/熔断异常 → 统一返回 JSON `Result<?>`（非默认的纯文本）/ Returns unified JSON instead of plain text
- 7 个业务模块 + gateway 均集成，双数据源纳管到 Nacos / All 7 business modules + gateway integrated, dual data sources managed via Nacos

### AOP 审计日志 / AOP Audit Logging（@AuditLog + AuditLogAspect + AuditLogService）

- `@AuditLog(module, operation, description)` 声明式注解 / Declarative annotation
- `AuditLogAspect` 环绕通知：记录操作人/IP/模块/操作/描述/耗时/异常 / Around advice: records operator, IP, module, operation, description, duration, exception
- `AuditLogService` 异步写入（`@Async`），不阻塞主流程 / Async write, non-blocking for main flow
- 已标注 Controller：AuthController、SupplierController、ContractController、PurchaseController、InventoryController、ReceiptController

### 限流注解 / Rate Limit Annotation（@RateLimit + RateLimitAspect）

- Guava RateLimiter 本地令牌桶 / Local token bucket
- `@RateLimit(value=10, timeout=1)` 每秒 10 个令牌 + 1 秒等待超时 / 10 tokens/sec + 1s timeout
- 本地兜底，不依赖 Sentinel Dashboard / Local fallback, independent of Sentinel Dashboard

### RedisDistributedLock（Redisson）

- `tryLock(lockKey, waitTime, leaseTime, () -> {...})` 模板方法 / Template method
- 自动续期（Watchdog）+ 自动释放 / Auto-renewal (Watchdog) + auto-release

### 消息队列 / Message Queue

- `AbstractMessageProducer`：RocketMQ 抽象生产者（同步/异步/单向发送）/ Abstract producer (sync/async/one-way)
- `AbstractMessageConsumer`：RocketMQ 抽象消费者 / Abstract consumer

### 公共实体 / Common Entities

- `Goods` / `GoodsCategory`：物料主数据与分类（雪花 ID + MyBatis-Plus `ASSIGN_ID`）/ Material master data & classification (Snowflake ID + MyBatis-Plus ASSIGN_ID)
- `AuditLog`：审计日志实体 / Audit log entity

---

## 数据权限模型 / Data Permission Model

`DataScopeInterceptor` 通过 AOP 切面读取 `role.data_scope` 和 `user.dept_id`，改写 SQL WHERE 条件：

---

`DataScopeInterceptor` reads `role.data_scope` and `user.dept_id` via AOP aspects to rewrite SQL WHERE conditions:

| 级别 / Level | `data_scope` | WHERE 条件 / Condition |
|------|-------------|------------|
| 仅本人 / Self Only | 1 | `created_by = currentUserId` |
| 本部门 / Own Department | 2 | `dept_id = currentDeptId` |
| 本部门及子部门 / Dept & Sub-depts | 3 | `dept_path LIKE '/1/3%'` |
| 全部 / All | 4 | 无限制 / Unlimited |
