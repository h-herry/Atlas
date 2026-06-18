# atlas-gateway — API 网关 / API Gateway

## 功能概述 / Overview

`atlas-gateway` 是系统对外的统一 API 入口，基于 Spring Cloud Gateway 实现。负责请求路由转发、全局 CORS、Nacos 服务发现、负载均衡、Sentinel 网关限流熔断、Zipkin 链路追踪。

---

`atlas-gateway` is the system's unified external API entry point, built on Spring Cloud Gateway. It handles request routing and forwarding, global CORS, Nacos service discovery, load balancing, Sentinel gateway rate limiting and circuit breaking, and Zipkin distributed tracing.

**端口 / Port**：8080 | **文件数 / File count**：1 Java class + 2 yml configuration files

---

## 路由规则 / Routing Rules

网关监听 8080 端口，将 7 条路由转发至对应的后端服务 / Gateway listens on port 8080, forwarding 7 routes to corresponding backend services：

| 路由 ID | 匹配规则 / Match Pattern | 转发目标 / Target | 说明 / Description |
|---------|----------|----------|------|
| atlas-user | `/api/user/**` | lb://atlas-user | 用户认证与权限 / User auth & permissions |
| atlas-supplier | `/api/supplier/**` | lb://atlas-supplier | 供应商 SRM + 物料管理 / Supplier SRM + Material |
| atlas-contract | `/api/contract/**` | lb://atlas-contract | 合同管理 / Contract management |
| atlas-purchase | `/api/purchase/**` | lb://atlas-purchase | 采购管理 / Purchase management |
| atlas-inventory | `/api/inventory/**` | lb://atlas-inventory | 库存管理 / Inventory management |
| atlas-receipt | `/api/receipt/**` | lb://atlas-receipt | 收货管理 / Receipt management |
| atlas-workflow | `/api/workflow/**` | lb://atlas-workflow | 工作流引擎 / Workflow engine |

> `lb://` 前缀表示通过 Spring Cloud LoadBalancer + Nacos 服务发现动态路由。 / The `lb://` prefix indicates dynamic routing via Spring Cloud LoadBalancer + Nacos service discovery.

---

## 配置详解 / Configuration Details

### 全局 CORS / Global CORS

```yaml
spring.cloud.gateway.globalcors.cors-configurations:
  '[/**]':
    allowed-origin-patterns: "*"
    allowed-methods: "*"
    allowed-headers: "*"
    allow-credentials: true
```

允许所有来源/方法/头的跨域请求，支持携带认证凭据。 / Allows all origins, methods, and headers for cross-origin requests with credentials support.

### Sentinel 网关限流 / Sentinel Gateway Rate Limiting

```yaml
spring.cloud.sentinel:
  transport.dashboard: localhost:8080
  datasource:
    ds1.nacos: atlas-gateway-flow-rules    # 流控规则 / Flow rules
    ds2.nacos: atlas-gateway-degrade-rules # 降级规则 / Degrade rules
```

网关层 Sentinel 规则通过 Nacos 动态下发，无需重启网关。 / Gateway-level Sentinel rules are dynamically delivered via Nacos without restart.

### Zipkin 链路追踪 / Zipkin Distributed Tracing

```yaml
management:
  tracing.sampling.probability: 1.0
  zipkin.tracing.endpoint: http://localhost:9411/api/v2/spans
```

采样率 100%，所有请求自动上报到 Zipkin。 / 100% sampling rate, all requests automatically reported to Zipkin.

### 默认过滤器 / Default Filters

```yaml
spring.cloud.gateway.default-filters:
  - AddRequestHeader=X-Gateway-Timestamp, #{T(java.time.LocalDateTime).now().toString()}
```

每个经过网关的请求自动添加 `X-Gateway-Timestamp` 头，用于链路追踪和超时分析。 / Each request passing through the gateway automatically gets an X-Gateway-Timestamp header for tracing and timeout analysis.

### 服务发现 / Service Discovery

```yaml
spring.cloud.gateway.discovery.locator:
  enabled: true
  lower-case-service-id: true
```

启用自动服务发现路由 + 服务名小写化。 / Enable auto service discovery routing with lowercase service IDs.

---

## 技术要点 / Technical Details

### 请求流转链路 / Request Flow Chain

```
客户端 → atlas-gateway:8080 → Nacos 服务发现 → LoadBalancer 负载均衡 → 目标服务
Client → atlas-gateway:8080 → Nacos discovery → LoadBalancer → Target service
```

### Sentinel 双数据源纳管 / Sentinel Dual Data Source Management

网关流控和降级规则均存储在 Nacos Config，通过 Sentinel Dashboard 修改规则后实时生效，无需重启网关。 / Both flow control and degrade rules are stored in Nacos Config, taking effect in real-time via Sentinel Dashboard without restart.

### 限流/熔断兜底 / Rate Limit / Circuit Break Fallback

当某条路由触发 Sentinel 限流或熔断规则时，`SentinelConfig` 的全局异常处理会返回 `RATE_LIMIT_EXCEEDED(429)` 或 `CIRCUIT_BREAKER_OPEN(12020)` JSON 响应，而非默认的纯文本 429 页面。 / When a route triggers Sentinel rules, the global exception handler returns JSON responses with proper error codes rather than default plain-text pages.
