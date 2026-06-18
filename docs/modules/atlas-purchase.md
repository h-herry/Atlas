# atlas-purchase — 采购管理 / Purchase Management

## 功能概述 / Overview

`atlas-purchase` 模块负责采购订单管理 + 9 种采购模式实现，是业务复杂度最高的模块之一。支持公开招标、邀请招标、竞争性谈判、竞争性磋商、询价、竞价/拍卖、单一来源、合作创新、框架协议采购等 9 种模式，每种有独立的 Controller/Service/Entity。

---

The `atlas-purchase` module handles purchase order management and implements 9 procurement modes, making it one of the most complex modules. It supports open bidding, invited bidding, competitive negotiation, competitive consultation, inquiry, auction/bidding, single-source, cooperative innovation, and framework agreement procurement—each with its own Controller/Service/Entity.

**端口 / Port**：8084 | **数据库 / Database**：atlas_purchase | **文件数 / File count**：68 Java classes | **Controller 数 / Controllers**：10

---

## 数据库表列表 / Database Tables

### 基础采购表 / Core Purchase Tables（2 张 / 2 tables）

| 表名 / Table | 说明 / Description | 迁移 / Migration |
|------|------|------|
| purchase_order | 采购订单主表（幂等校验 uk_request_id） / Purchase order master (idempotent check via uk_request_id) | V4 |
| order_item | 采购明细表 / Order item detail | V4 |

### 9 种采购模式扩展表 / 9 Procurement Mode Extension Tables（20+ 张，待补录 / 20+ tables, pending）

| 模式 / Mode | 表 / Tables | 说明 / Description |
|------|-----|------|
| 公开招标 / Open Bidding | open_bidding、open_bidding_supplier | 招标发布 + 供应商投标 / Bid release + supplier bidding |
| 邀请招标 / Invited Bidding | invited_bidding、invited_bidding_supplier | 邀请特定供应商 / Invite specific suppliers |
| 竞争性谈判 / Competitive Negotiation | negotiation_session、negotiation_round | 谈判会话 + 多轮报价 / Negotiation session + multi-round quotes |
| 竞争性磋商 / Competitive Consultation | consultation_session、consultation_review | 磋商会话 + 评审 / Consultation session + review |
| 询价 / Inquiry | inquiry_purchase、inquiry_supplier | 询价发布 + 供应商报价 / Inquiry release + supplier quotes |
| 竞价/拍卖 / Auction | auction_purchase、auction_bid | 竞价场次 + 出价记录 / Auction session + bid records |
| 单一来源 / Single Source | single_source_purchase | 直接指定供应商 / Direct supplier designation |
| 合作创新 / Cooperative Innovation | cooperative_innovation | 研发合作 + 首购 / R&D collaboration + first purchase |
| 框架协议 / Framework Agreement | framework_agreement、framework_supplier、framework_order | 协议 + 供应商入围 + 二次下单 / Agreement + supplier shortlisting + secondary ordering |

---

## Controller API 列表 / Controller API List

### 一、采购订单 — PurchaseController（/api/purchase） / Purchase Order

| 端点 / Endpoint | 方法 | 权限 / Permission | 说明 / Description |
|------|------|------|------|
| `/api/purchase/order` | POST | purchase:manage | 创建采购订单 / Create purchase order |
| `/api/purchase/order/{id}/submit` | PUT | purchase:manage | 提交订单（幂等校验 + 库存扣减） / Submit order (idempotent check + stock deduction) |
| `/api/purchase/order/{id}` | GET | purchase:view | 查询订单详情 / Query order detail |
| `/api/purchase/order/page` | GET | purchase:view | 分页查询订单 / Paginated order query |
| `/api/purchase/order/{id}/items` | GET | purchase:view | 查询订单明细 / Query order items |

### 二、公开招标 — OpenBiddingController（/api/purchase/open-bidding） / Open Bidding

| 端点 / Endpoint | 方法 | 说明 / Description |
|------|------|------|
| `/api/purchase/open-bidding/page` | GET | 分页查询招标 / Paginated bidding query |
| `/api/purchase/open-bidding/{id}` | GET | 查询招标详情 / Query bidding detail |
| `/api/purchase/open-bidding/{id}/suppliers` | GET | 查询投标供应商 / Query bidding suppliers |
| `/api/purchase/open-bidding` | POST | 发布招标公告 / Publish bidding announcement |
| `/api/purchase/open-bidding/{id}/start` | PUT | 启动招标 / Start bidding |
| `/api/purchase/open-bidding/{id}/bid` | POST | 供应商投标 / Supplier bid |
| `/api/purchase/open-bidding/{id}/open` | PUT | 开标 / Open bids |
| `/api/purchase/open-bidding/{id}/evaluate` | POST | 评标打分 / Evaluate and score |
| `/api/purchase/open-bidding/{id}/award` | PUT | 定标 / Award contract |
| `/api/purchase/open-bidding/{id}/flow` | PUT | 流标 / Cancel bidding (no winner) |
| `/api/purchase/open-bidding/{id}/terminate` | PUT | 终止招标 / Terminate bidding |

### 三~十、其他采购模式控制器 / Other Procurement Mode Controllers

- **InvitedBiddingController**（/api/purchase/invited-bidding）：与公开招标流程相似 / Similar to open bidding flow
- **NegotiationController**（/api/negotiation）：创建谈判会话 → 供应商参与 → 提交报价（多轮 round）→ 综合评审 → 最终定价 / Create session → supplier participation → multi-round quotes → comprehensive review → final pricing
- **ConsultationController**（/api/consultation）：创建磋商会话 → 供应商提交响应文件 → 综合评审 → 确定成交 / Create session → supplier response submission → comprehensive review → confirm deal
- **InquiryController**（/api/purchase/inquiry）：发布询价 → 供应商报价 → 确认结果 / Publish inquiry → supplier quotes → confirm results
- **AuctionController**（/api/purchase/auction）：创建竞价场次 → 供应商出价 → 结果确认 / Create auction → supplier bids → confirm results
- **SingleSourceController**（/api/purchase/single-source）：直接指定供应商采购（需论证文件） / Direct supplier designation (requires justification)
- **CooperativeInnovationController**（/api/purchase/cooperative-innovation）：研发合作创建 → 首购申请 → 推广应用 / R&D collaboration → first purchase application → promotion
- **FrameworkController**（/api/framework）：协议签订 → 供应商入围 → 二次竞价下单 / 直接下单 / Agreement signing → supplier shortlisting → secondary bidding / direct ordering

---

## 技术要点 / Technical Details

### 采购订单幂等校验 / Purchase Order Idempotent Check

`purchase_order` 表 `uk_request_id` 唯一索引，重复提交相同 `requestId` 的订单会触发数据库唯一约束冲突，由全局异常处理返回 `ORDER_DUPLICATE(4003)`。 / The uk_request_id unique index on purchase_order catches duplicate submissions; global exception handler returns ORDER_DUPLICATE(4003).

### 提交订单库存扣减 / Order Submission Stock Deduction

`submitOrder()` 内部调用 `atlas-inventory` 模块的库存扣减接口，使用乐观锁 + 自旋重试确保并发安全。库存不足时返回 `STOCK_INSUFFICIENT(5001)`。 / submitOrder() internally calls atlas-inventory's stock deduction API, using optimistic locking + spin retry for concurrency safety. Returns STOCK_INSUFFICIENT(5001) on shortage.

### 服务间调用 / Inter-Service Communication

通过 Spring Cloud LoadBalancer + Nacos 服务发现调用 `atlas-inventory`，替代了原有的 `RestTemplate` 硬编码调用。 / Uses Spring Cloud LoadBalancer + Nacos service discovery to call atlas-inventory, replacing hardcoded RestTemplate calls.
