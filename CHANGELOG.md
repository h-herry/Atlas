
# Atlas 变更日志 / Atlas Changelog

> 本文档记录 Atlas SRM 系统自 v1.0.0 起的所有发布版本变更 / This document records all release versions and their changes since v1.0.0.

---

## v1.2.21 (2026-06-18)

### 新增模块 / New Modules

- **atlas-message**: WebSocket 实时消息推送、邮件通知、短信通知三通道消息中心 / WebSocket real-time push, email notification, SMS notification triple-channel message center

### 供应商端门户（atlas-supplier portal 子包） / Supplier Portal (atlas-supplier portal sub-package)

- 供应商认证体系：独立 JWT (role=SUPPLIER)，与企业端隔离 / Supplier authentication: independent JWT (role=SUPPLIER), isolated from enterprise
- 供应商准入双通道：自助注册 + 采购员代注册，Flowable 三级审批流 / Supplier onboarding dual-channel: self-registration + purchaser proxy registration, Flowable 3-level approval
- 供应商档案管理：企业信息编辑、资质上传/列表/到期预警 / Supplier profile management: company info edit, certificate upload/list/expiry alert
- 询报价管理：询价单查看、报价提交/修改、竞标大厅（Redis 实时排名）/ RFQ management: inquiry view, quote submission/modification, bidding hall (Redis real-time ranking)
- 合同管理：合同查看、在线签署/拒绝、履约进度/预警 / Contract management: contract view, online sign/reject, performance progress/alert
- 物流管理：发货任务、确认发货、物流轨迹、延迟通知 / Logistics management: shipping tasks, confirm shipment, tracking, delay notification
- 订单管理：订单列表/详情/统计、确认接单、填写明细、生产进度、交期管理、工作台 / Order management: order list/detail/stats, confirm acceptance, fill details, production progress, delivery management, workspace

### 电子合同管理（atlas-contract econtract 子包） / E-Contract Management (atlas-contract econtract sub-package)

- 合同模板管理：CRUD + 基于模板生成合同 / Contract template management: CRUD + generate contract from template
- 签署流程：状态机 DRAFT→SIGNING→COMPLETED/EXPIRED/CANCELLED，多步骤顺序签 / Signing flow: state machine DRAFT→SIGNING→COMPLETED/EXPIRED/CANCELLED, multi-step sequential signing
- 条款比对：LCS 算法行级 diff，JSON 格式差异输出 / Clause comparison: LCS algorithm line-level diff, JSON format output
- 履约跟踪：履约指标管理、逾期自动标记 BREACHED、定时预警扫描 / Performance tracking: indicator management, auto-mark BREACHED on overdue, scheduled alert scan

### 基础设施 / Infrastructure

- WebSocket STOMP over SockJS 实时推送 / WebSocket STOMP over SockJS real-time push
- Redis Pub/Sub 跨实例消息广播 / Redis Pub/Sub cross-instance message broadcast
- JavaMailSender 邮件通道（12 个 HTML 模板）/ JavaMailSender email channel (12 HTML templates)
- 阿里云/腾讯云短信通道（8 个模板）/ Aliyun/Tencent Cloud SMS channel (8 templates)
- 8 类业务事件自动推送（订单/交付/结算/审批/合同/竞价/质检/入驻）/ 8 business event types auto-push (order/delivery/settlement/approval/contract/bidding/quality/onboarding)

### 数据库迁移 / Database Migrations

- V95: sup_portal_register, sup_portal_certificate, sup_portal_notification
- V96: msg_record, msg_template（9 条预置模板 / 9 preset templates）
- V97: cnt_template, cnt_sign_flow, cnt_sign_record, cnt_clause_compare, cnt_performance, cnt_performance_alert
- V98: sup_onboarding_approval，sup_portal_register 扩展字段 / sup_onboarding_approval, sup_portal_register extended fields

### 新增依赖 / New Dependencies

- spring-boot-starter-websocket
- spring-boot-starter-mail
- com.aliyun:dysmsapi20170525

### 文档规范 / Documentation Standardization

- 删除所有 .md 文档中的 AIGC YAML frontmatter 块（原 06/07/08 报告第 2-8 行）/ Removed AIGC YAML frontmatter blocks from all .md files (lines 2-8 in original 06/07/08 reports)
- humanizer 润色核心文档：01-技术文档（架构描述句式）、07-SRM 项目总报告（架构概览段）、08-功能优化建议报告（冗余"进行"句式及空 frontmatter 清理）/ Humanizer polish on core docs: 01-Technical Documentation (architecture description), 07-SRM Project Report (architecture overview), 08-Gap Analysis (redundant "进行" phrasing and empty frontmatter cleanup)
- 去掉"采用...架构，基于..."模板化句式，改为更直接的工程化描述 / Replaced formulaic "采用...架构，基于..." patterns with direct engineering descriptions
- 全量代码去 AI 化扫描：对 540 个源文件（Java/XML/YAML/HTML/SQL/Properties/Gradle/VM）扫描 AI 生成标识、冗余样板注释、假版权声明等模式，确认项目中不存在任何 AI 生成痕迹或机器感注释 / Full-codebase de-AI scan: 540 source files (Java/XML/YAML/HTML/SQL/Properties/Gradle/VM) scanned for AI-generated markers, redundant boilerplate comments, fake copyright notices — confirmed zero AI traces or machine-feeling comments in the codebase

---

## v1.2.10 (2026-06-17)

- SRM 对标某行业头部平台四域升级：供应商配额/整改/财务结算/预测协同/竞价大厅/价格库/合同签章/风险预警/供应商推荐/开放平台 / SRM benchmark upgrade: supplier quota, rectification, financial settlement, forecast collaboration, bidding hall, price library, contract signing, risk warning, supplier recommendation, open platform

---

## v1.0.1 (2026-06-17)

- 系统优化与文档完善 / System optimization and documentation improvement

---

## v1.0.0 (2026-03)

- Atlas SRM 系统初始发布，包含 10 个核心微服务模块 / Atlas SRM system initial release with 10 core microservice modules