

# Atlas — SRM 供应商关系管理系统 / Supplier Relationship Management

Atlas 是面向中大型企业的一站式 SRM 系统，覆盖供应商全生命周期管理，对标某行业头部平台能力，支持企业端与供应商端双端协作。

Atlas is a full-lifecycle SRM system for mid-to-large enterprises, benchmarking top-tier industry platforms. It supports dual-portal collaboration between enterprise and supplier sides.

## 技术栈 / Tech Stack

Sprint Boot 3 / Spring Cloud / MyBatis-Plus / Flowable / Redis / WebSocket / MySQL

## 模块架构 / Modules

| Module | Port | Description |
|--------|------|-------------|
| atlas-common | - | 公共基础设施，含 WebSocket 推送、消息模板 / Common infrastructure: WebSocket push, message templates |
| atlas-gateway | 8080 | API 网关，统一鉴权路由 / API gateway, unified auth & routing |
| atlas-auth | - | 认证授权，双通道 JWT（企业端 + 供应商端） / Dual-channel JWT auth |
| atlas-system | - | 系统管理，用户/角色/菜单/字典 / System admin: users, roles, menus, dicts |
| atlas-material | - | 物料主数据管理 / Material master data |
| atlas-supplier | - | 供应商管理，含企业端 & 供应商门户端 / Supplier management, enterprise + supplier portal |
| atlas-inquiry | - | 询报价管理，含竞价大厅 / RFQ management, bidding hall |
| atlas-order | - | 采购订单管理 / Purchase order management |
| atlas-delivery | - | 交付物流跟踪 / Delivery & logistics tracking |
| atlas-settlement | - | 结算对账 / Settlement & reconciliation |
| atlas-quality | - | 质量检验管理 / Quality inspection |
| atlas-contract | - | 合同管理，含电子合同签署 / Contract management, e-signature |
| atlas-message | 8097 | 消息中心，WebSocket + 邮件 + 短信三通道 / Message center, 3-channel notification |

## 核心特性 / Key Features

**供应商门户 / Supplier Portal**

自助注册、准入审批（Flowable 工作流）、资质管理、订单协同、竞价参与。

Self-service registration, admission approval via Flowable, qualification management, order collaboration, bidding participation.

**电子合同 / E-Contract**

模板管理、在线签署、LCS 条款比对、履约预警。

Template management, online signing, LCS clause comparison, performance alerts.

**消息通道 / Messaging**

WebSocket 实时推送 + 邮件通知（12 模板） + 短信通知（8 模板），Redis Pub/Sub 跨实例广播。

Real-time WebSocket push + 12 email templates + 8 SMS templates, Redis Pub/Sub cross-instance broadcast.

**询报价引擎 / RFQ Engine**

Redis Sorted Set 实时竞标排名，供应商匿名竞价。

Real-time bidding ranking via Redis Sorted Set, anonymous supplier bidding.

**认证与隔离 / Auth & Isolation**

企业端 + 供应商端双通道 JWT 认证，supplier_id 级数据隔离。

Dual-channel JWT auth with supplier_id-level data isolation.

## 快速开始 / Quick Start

**环境要求 / Prerequisites**

- JDK 17+
- MySQL 8.0+
- Redis
- Maven 3.8+

**构建启动 / Build & Run**

```bash
# 克隆
git clone https://github.com/h-herry/Atlas.git
cd Atlas

# 构建
mvn clean install -DskipTests

# 启动 gateway
cd atlas-gateway && mvn spring-boot:run
```

默认访问地址：`http://localhost:8080`

供应商门户入口：`http://localhost:{port}/portal`

Supplier portal entry: `http://localhost:{port}/portal`

## 版本历史 / Version History

| Version | Date | Highlights |
|---------|------|------------|
| v1.2.23 | 2026-06-18 | 技术优化：防重放攻击/敏感脱敏/审计日志/异常标准化/Cache-Aside缓存/复合索引/异步化/健康检查/结构化日志/重试死信/API文档；制造业场景：JIT/VMI/PPAP/多工厂分单/来料追溯；数据库V99-V106共46张新表 / Tech optimization & manufacturing scenarios, DB V99-V106, 46 new tables |
| v1.2.21 | 2026-06-18 | 供应商门户 + 电子合同 + 三通道消息中心 / Supplier portal, e-contract, 3-channel messaging |
| v1.2.10 | - | 基础 SRM 功能框架 / SRM core framework |
| v1.0.1 | - | 项目初始化 / Project initialization |
| v1.0.0 | - | 项目立项 / Project kick-off |

## 文档 / Documentation

| 文档 | 说明 |
|------|------|
| [01-技术文档](docs/01-技术文档.md) | 技术架构与设计说明 / Technical architecture |
| [02-项目说明书](docs/02-项目说明书.md) | 项目背景与需求 / Project overview |
| [03-数据库文档](docs/03-数据库文档.md) | 数据库设计与表结构 / Database schema |
| [04-期初数据](docs/04-期初数据.md) | 初始化数据说明 / Initial data |
| [05-代码分析报告](docs/05-代码分析报告.md) | 代码质量分析 / Code analysis |
| [06-SRM项目总报告](docs/06-SRM项目总报告.md) | SRM 项目总报告 / SRM report |
| [07-SRM项目总报告-v1.2.23](docs/07-SRM项目总报告-v1.2.23.md) | v1.2.23 版本报告 / v1.2.23 release report |
| [08-功能优化建议报告-v1.2.23](docs/08-功能优化建议报告-v1.2.23.md) | 优化建议 / Optimization suggestions |

## 许可 / License

MIT
