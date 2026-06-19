# Atlas SRM

Atlas 企业供应商关系管理系统 v1.2.502 —— 覆盖供应商全生命周期的微服务架构平台。

---

## 模块架构

| 模块 | 端口 | 说明 |
|------|:---:|------|
| atlas-common | - | 公共基础设施：实体/Mapper/安全/缓存/消息中心/WebSocket |
| atlas-gateway | 8080 | API 网关：路由转发 / 鉴权校验 / 限流 / 负载均衡 |
| atlas-user | 8081 | 用户服务 + RBAC 权限管理（用户-角色-权限+六级数据范围） |
| atlas-supplier | 8082 | 供应商管理：准入/档案/评估/黑名单/资质 |
| atlas-workflow | 8083 | Flowable 工作流引擎：审批流程 / 自定义流程 |
| atlas-purchase | 8084 | 采购管理：采购订单 / 询报价 / 竞价 / 价格主数据 |
| atlas-inventory | 8085 | 库存管理：入库/出库/调拨/盘点/安全库存 |
| atlas-contract | 8086 | 合同管理：电子合同签署 / 审批 / 履约跟踪 |
| atlas-material | 8088 | 物料主数据：物料编码/分类/规格/批次追溯 |
| atlas-receipt | 8089 | 收货管理：交付物流 / 收货验收 / 结算对账 / 三单匹配 |
| atlas-quality | 8091 | 质量检验：来料检验 / 过程检验 / 不合格品处理 |
| atlas-open | 8719 | 开放平台 API：供应商门户对接 / 第三方系统集成 |

> 12 模块，11 个端口。atlas-common 为基础设施模块，不占用端口。
> RBAC 权限体系（`SysPermission` / `SysRole` / `SysUserRole` 等实体）位于 `com.atlas.common.entity`，user 模块通过 `com.atlas.user.system` 子包提供 Controller 和 Service。

---

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot / Spring Cloud | 3.2.5 / 2023.0.1 |
| 微服务治理 | Spring Cloud Alibaba（Nacos） | 2023.0.1.0 |
| 数据持久化 | MyBatis-Plus + MySQL | 3.5.6 / 8.0 |
| 工作流 | Flowable | 7.0.1 |
| 缓存 | Redis（Redisson） | Redisson 3.27.2 |
| 消息队列 | RocketMQ | 5.1.4 |
| 网关 | Spring Cloud Gateway | 2023.0.1 |
| 实时通信 | WebSocket（STOMP） | - |
| 分布式事务 | Seata | 1.8.0 |
| 安全认证 | JJWT 双通道 JWT | 0.12.5 |
| 接口文档 | Knife4j（Swagger） | 4.5.0 |
| JSON | Fastjson2 | 2.0.53 |
| 工具库 | Lombok / Hutool | 1.18.38 / 5.8.27 |
| 运行环境 | JDK 17 / Maven 3.8+ | - |

---

## 快速开始

### 环境要求

| 依赖 | 版本 |
|------|------|
| JDK | 17 |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| Maven | 3.8+ |
| Nacos | 2.3+（服务注册/配置中心，按需） |

### 编译

```bash
cd /Users/h/IdeaProjects/atlas
mvn clean compile -T 1C
```

### 启动单个模块

```bash
mvn spring-boot:run -pl <module>
```

示例：

```bash
mvn spring-boot:run -pl atlas-gateway
mvn spring-boot:run -pl atlas-user
```

### 启动顺序

1. Nacos（如启用）
2. Redis / MySQL
3. atlas-gateway（8080）
4. atlas-user（8081）
5. 其余业务模块（按需）

---

## 项目结构

```
atlas/
├── atlas-common/         公共基础设施（entity / mapper / security / message / websocket）
├── atlas-gateway/        API 网关（路由转发 / 鉴权 / 限流）
├── atlas-user/           用户服务 + RBAC 权限管理
├── atlas-supplier/       供应商管理
├── atlas-workflow/       Flowable 工作流引擎
├── atlas-purchase/       采购管理（订单 / 询报价 / 价格主数据）
├── atlas-inventory/      库存管理
├── atlas-contract/       合同管理
├── atlas-material/       物料主数据 + 批次追溯
├── atlas-receipt/        收货管理 / 交付物流 / 结算对账 / 三单匹配
├── atlas-quality/        质量检验
├── atlas-open/           开放平台 API
├── docs/                 项目文档
│   ├── 00-文档规范.md
│   ├── 01-技术文档.md
│   ├── 02-系统设计文档.md
│   ├── 03-数据库文档.md
│   └── 04-项目审计报告.md
├── sql/                  SQL 迁移脚本
├── docker/               Docker 编排
├── pom.xml               父 POM（12 模块聚合）
└── .env.example          环境变量模板
```

每个业务模块标准包结构：

```
atlas-<module>/
└── src/main/java/com/atlas/<module>/
    ├── <Module>Application.java   启动类
    ├── config/                    模块配置
    ├── controller/                REST 控制器
    ├── entity/                    实体类（模块专属）
    ├── mapper/                    MyBatis Mapper
    ├── service/                   业务逻辑
    ├── dto/                       数据传输对象
    ├── model/                     领域模型
    └── feat/                      特性子包（purchase → order/inquiry/price/settlement）
```

---

## 文档索引

| 文档 | 路径 |
|------|------|
| 技术文档 | [01-技术文档.md](docs/01-技术文档.md) |
| 系统设计文档 | [02-系统设计文档.md](docs/02-系统设计文档.md) |
| 数据库文档 | [03-数据库文档.md](docs/03-数据库文档.md) |
| 项目审计报告 | [04-项目审计报告.md](docs/04-项目审计报告.md) |

---

## 合并历史

| 变更 | 说明 |
|------|------|
| system → user | 原 `atlas-system`（RBAC 权限管理）合并入 `atlas-user`，作为 `com.atlas.user.system` 子包，端口保持 8081 |
| message → common | 原 `atlas-message`（消息中心/WebSocket）合并入 `atlas-common` |
| delivery → receipt | 原 `atlas-delivery`（交付物流）合并入 `atlas-receipt`，作为 `com.atlas.receipt.delivery` 子包 |
| order → purchase | 原 `atlas-order`（采购订单）合并入 `atlas-purchase`，作为 `com.atlas.purchase.order` 子包 |

---

## 版本历史

> 详见 [CHANGELOG.md](CHANGELOG.md)

| 版本 | 日期 | 变更摘要 |
|------|------|------|
| v1.2.502 | 2026-06-19 | system→user 合并（13→12 模块）、material 补充 Mapper/XML 持久层、四份核心文档重生成 |
| v1.2.401 | 2026-06-18 | 模块合并（16→13）：message→common / delivery→receipt / order→purchase；功能扩充；37 处 @Transactional 补 rollbackFor |
| v1.2.24 | 2026-06-18 | 全量合规检查通过、16 处编译错误修复 |
| v1.2.23 | 2026-06-18 | 防重放攻击、数据脱敏、审计日志、异常标准化、缓存策略、异步批量、结构化日志、制造业场景（JIT/VMI/PPAP/多工厂分单/批次追溯） |
| v1.2.21 | 2026-06-18 | 消息中心（WebSocket/邮件/短信）、供应商门户（准入/询报价/合同/物流/订单）、电子合同管理（签署/条款比对/履约跟踪） |
| v1.2.10 | 2026-06-17 | SRM 四域升级：配额/结算/竞价/价格库/合同签章/风险预警/开放平台 |
| v1.0.1 | 2026-06-17 | 系统优化与文档完善 |
| v1.0.0 | 2026-03 | 初始发布：10 核心微服务模块 |
