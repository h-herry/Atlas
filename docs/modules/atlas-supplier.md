# atlas-supplier — 供应商关系管理 SRM + 物料管理 / Supplier Relationship Management + Material Management

## 功能概述 / Overview

`atlas-supplier` 是系统中复杂度最高的模块，负责供应商全生命周期管理和物料管理两大核心域。SRM 覆盖准入、评估、协同、风险四个功能域，物料管理覆盖基础数据、需求计划、采购协同、质量追溯四个子模块。

---

`atlas-supplier` is the most complex module in the system, responsible for two core domains: supplier lifecycle management and material management. SRM covers four functional areas—access, evaluation, collaboration, and risk—while material management covers four sub-modules—master data, demand planning, procurement collaboration, and quality traceability.

**端口 / Port**：8082 | **数据库 / Database**：atlas_supplier | **文件数 / File count**：90 Java classes | **Controller 数 / Controllers**：15

---

## 数据库表列表 / Database Tables

### SRM 核心表 / SRM Core Tables（15 张已记录 / 15 recorded）

| 表名 / Table | 功能域 / Domain | 迁移 / Migration |
|------|--------|------|
| supplier | 供应商主表（含 SRM 扩展字段） / Supplier master (with SRM extension fields) | V2 |
| supplier_qualification | 供应商资质 / Supplier qualification | V2 |
| recruit_notice | 招募公告 / Recruitment notice | V11 待补录 / TBD |
| supplier_register | 准入申请 / Access application | V11 待补录 / TBD |
| supplier_approval_record | 审批记录 / Approval record | V11 待补录 / TBD |
| eval_template | 评估模板 / Evaluation template | V12 待补录 / TBD |
| supplier_evaluation | 绩效考核 / Performance evaluation | V12 待补录 / TBD |
| supplier_evaluation_item | 评估明细 / Evaluation item detail | V12 待补录 / TBD |
| forecast_notice | 预测计划 / Forecast plan | V13 待补录 / TBD |
| delivery_order | 发货单 / Delivery order | V13 待补录 / TBD |
| reconciliation | 对账单 / Reconciliation statement | V13 待补录 / TBD |
| risk_event | 风险事件 / Risk event | V14 待补录 / TBD |
| alert_rule | 预警规则 / Alert rule | V14 待补录 / TBD |
| alert_record | 预警记录 / Alert record | V14 待补录 / TBD |
| supplier_blacklist | 黑名单 / Blacklist | V14 待补录 / TBD |

### 物料管理表 / Material Management Tables（共享 goods 实体 + 12 张物料扩展表 / Shared goods entity + 12 material extension tables）

| 表名 / Table | 功能域 / Domain | 迁移 / Migration |
|------|--------|------|
| goods | 物料主数据（atlas-common 实体） / Material master data (atlas-common entity) | V8 |
| goods_category | 物料分类树 / Material category tree | V8 |
| material_unit | 多单位换算 / Multi-unit conversion | 待补录 / TBD |
| bom | BOM 物料清单 / Bill of Materials | 待补录 / TBD |
| mrp_plan | MRP 计划 / MRP plan | 待补录 / TBD |
| supplier_delivery | 供应商发货协同 / Supplier delivery collaboration | 待补录 / TBD |
| production_progress | 生产进度 / Production progress | 待补录 / TBD |
| iqc_inspection | 来料检验 / Incoming quality inspection | 待补录 / TBD |
| material_trace | 物料追溯 / Material traceability | 待补录 / TBD |
| supplier_performance | 供应商绩效 / Supplier performance | 待补录 / TBD |
| material_analysis | 物料分析报表 / Material analysis report | 待补录 / TBD |

---

## Controller API 列表 / Controller API List

### 一、供应商主数据 — SupplierController（/api/supplier） / Supplier Master Data

| 端点 / Endpoint | 方法 | 权限 / Permission | 说明 / Description |
|------|------|------|------|
| `/api/supplier/page` | GET | supplier:view | 分页查询（含关键字搜索） / Paginated query with keyword search |
| `/api/supplier/{id}` | GET | supplier:view | 根据 ID 查询 / Query by ID |
| `/api/supplier` | POST | supplier:add | 新增供应商（@AuditLog） / Add supplier |
| `/api/supplier` | PUT | supplier:edit | 更新供应商 / Update supplier |
| `/api/supplier/{id}` | DELETE | supplier:del | 删除供应商 / Delete supplier |

### 二、SRM 准入管理 — SupplierAccessController（/api/supplier/access） / SRM Access Management

| 端点 / Endpoint | 方法 | 权限 / Permission | 说明 / Description |
|------|------|------|------|
| `/api/supplier/access/notice` | POST | supplier:access:add | 发布招募公告 / Publish recruitment notice |
| `/api/supplier/access/notice/page` | GET | supplier:access:view | 分页查询公告 / Paginated notice query |
| `/api/supplier/access/notice/{id}/close` | PUT | supplier:access:approve | 关闭公告 / Close notice |
| `/api/supplier/access/register` | POST | supplier:access:add | 供应商提交注册申请 / Submit registration |
| `/api/supplier/access/register/page` | GET | supplier:access:view | 分页查询注册申请 / Paginated registration query |
| `/api/supplier/access/register/{id}/initial-review` | POST | supplier:access:approve | 初审 / Initial review |
| `/api/supplier/access/register/{id}/field-inspect` | POST | supplier:access:approve | 现场考察 / Field inspection |
| `/api/supplier/access/register/{id}/final-review` | POST | supplier:access:approve | 终审 / Final review |

### 三、SRM 绩效评估 — SupplierEvaluationController（/api/supplier/eval） / SRM Performance Evaluation

| 端点 / Endpoint | 方法 | 权限 / Permission | 说明 / Description |
|------|------|------|------|
| `/api/supplier/eval/template` | POST | supplier:eval:add | 创建评估模板 / Create evaluation template |
| `/api/supplier/eval/template/page` | GET | supplier:eval:view | 分页查询模板 / Paginated template query |
| `/api/supplier/eval` | POST | supplier:eval:add | 生成绩效考核 / Generate performance evaluation |
| `/api/supplier/eval/{id}/scores` | POST | supplier:eval:add | 打分提交 / Submit scores |
| `/api/supplier/eval/{id}/confirm` | PUT | supplier:eval:confirm | 供应商确认 / Supplier confirmation |
| `/api/supplier/eval/{id}/improve/start` | PUT | supplier:eval:add | 发起整改 / Initiate rectification |
| `/api/supplier/eval/{id}/improve/complete` | PUT | supplier:eval:add | 整改完成 / Rectification complete |
| `/api/supplier/eval/page` | GET | supplier:eval:view | 分页查询评估 / Paginated evaluation query |

### 四、SRM 供需协同 — SupplierCollaborationController（/api/supplier/collab） / SRM Supply-Demand Collaboration

| 端点 / Endpoint | 方法 | 权限 / Permission | 说明 / Description |
|------|------|------|------|
| `/api/supplier/collab/forecast` | POST | supplier:collab:add | 发布预测计划 / Publish forecast |
| `/api/supplier/collab/forecast/page` | GET | supplier:collab:view | 分页查询预测 / Paginated forecast query |
| `/api/supplier/collab/delivery` | POST | supplier:collab:add | 创建发货单 / Create delivery order |
| `/api/supplier/collab/delivery/{id}/logistics` | PUT | supplier:collab:add | 更新物流信息 / Update logistics |
| `/api/supplier/collab/delivery/{id}/arrival` | PUT | supplier:collab:confirm | 到货确认 / Confirm arrival |
| `/api/supplier/collab/delivery/page` | GET | supplier:collab:view | 分页查询发货单 / Paginated delivery query |
| `/api/supplier/collab/reconciliation` | POST | supplier:collab:add | 生成对账单 / Generate reconciliation |
| `/api/supplier/collab/reconciliation/{id}/supplier-confirm` | PUT | supplier:collab:confirm | 供应商确认 / Supplier confirm |
| `/api/supplier/collab/reconciliation/{id}/purchaser-confirm` | PUT | supplier:collab:confirm | 采购方确认 / Purchaser confirm |
| `/api/supplier/collab/reconciliation/{id}/invoice` | PUT | supplier:collab:add | 标记已开票 / Mark invoiced |
| `/api/supplier/collab/reconciliation/page` | GET | supplier:collab:view | 分页查询对账单 / Paginated reconciliation query |

### 五、SRM 风险管理 — SupplierRiskController（/api/supplier/risk） / SRM Risk Management

| 端点 / Endpoint | 方法 | 权限 / Permission | 说明 / Description |
|------|------|------|------|
| `/api/supplier/risk/event` | POST | supplier:risk:manage | 创建风险事件 / Create risk event |
| `/api/supplier/risk/event/{id}/handle` | PUT | supplier:risk:manage | 开始处理 / Start handling |
| `/api/supplier/risk/event/{id}/resolve` | PUT | supplier:risk:manage | 闭环处理 / Close and resolve |
| `/api/supplier/risk/event/page` | GET | supplier:risk:view | 分页查询风险事件 / Paginated risk event query |
| `/api/supplier/risk/blacklist` | POST | supplier:risk:manage | 加入黑名单 / Add to blacklist |
| `/api/supplier/risk/blacklist/{supplierId}/remove` | PUT | supplier:risk:manage | 解除黑名单 / Remove from blacklist |
| `/api/supplier/risk/blacklist/check/{supplierId}` | GET | supplier:risk:view | 黑名单校验 / Blacklist check |
| `/api/supplier/risk/blacklist/page` | GET | supplier:risk:view | 分页查询黑名单 / Paginated blacklist query |

### 六、供应商预警 — SupplierAlertController（/api/supplier/alert） / Supplier Alert

预警规则 CRUD + 预警记录查询 + 规则状态切换 / Alert rule CRUD + alert record query + rule status toggle

### 七、物料主数据 — MaterialController（/api/material） / Material Master Data

| 端点 / Endpoint | 方法 | 权限 / Permission | 说明 / Description |
|------|------|------|------|
| `/api/material` | POST | material:manage | 新增物料 / Add material |
| `/api/material` | PUT | material:manage | 更新物料 / Update material |
| `/api/material/{id}` | GET | material:view | 查询物料 / Query material |
| `/api/material/code/{code}` | GET | material:view | 按编码查询 / Query by code |
| `/api/material/page` | GET | material:view | 分页查询（关键词/分类/类型/状态） / Paginated query (keyword/category/type/status) |
| `/api/material/{id}/status` | PUT | material:manage | 切换启用/禁用 / Toggle enabled/disabled |
| `/api/material/category` | POST | material:manage | 新增分类 / Add category |
| `/api/material/category/page` | GET | material:view | 分页查询分类 / Paginated category query |

### 八~十五、其他控制器 / Other Controllers

- **BomController**（/api/material/bom）：BOM 版本管理、发布、成本预估 / BOM version management, publishing, cost estimation
- **MrpController**（/api/material/mrp）：MRP 计划生成、确认、净需求计算 / MRP plan generation, confirmation, net requirement calculation
- **SupplierDeliveryController**（/api/material/delivery）：发货、收货确认、发货明细 / Shipping, receipt confirmation, delivery items
- **ProductionProgressController**（/api/material/progress）：生产进度填报、查询、状态更新 / Production progress reporting, query, status update
- **IqcInspectionController**（/api/material/iqc）：来料检验（合格/缺陷判定） / Incoming quality inspection (qualified/defective judgment)
- **MaterialTraceController**（/api/material/trace）：按批次号/条码追溯 / Trace by batch number or barcode
- **SupplierPerformanceController**（/api/material/performance）：按物料维度统计供应商交付与质量表现 / Supplier delivery & quality performance by material
- **MaterialAnalysisController**（/api/material/analysis）：物料分析报表保存/查询 / Material analysis report save/query

---

## 技术要点 / Technical Details

### 准入三级审批流程 / Three-Tier Access Approval Process

```
招募公告发布 → 供应商注册 → 初审（资质审核）→ 现场考察 → 终审 → 自动入库（supplier 状态变更）
Recruitment notice → Supplier registration → Initial review → Field inspection → Final review → Auto admission (supplier status change)
```

- `supplier_approval_record` 表记录每一步审批历史（审批人、结果、评分、意见） / Records each step's approval history (approver, result, score, comments)
- 终审通过自动更新 `supplier` 表中 SRM 扩展字段 / Auto-update supplier SRM extension fields upon final approval

### 绩效评估流程 / Performance Evaluation Process

```
创建评估模板 → 按模板生成评估 → 多维打分 → 自动计算总分 → 自动定级 A/B/C/D → 供应商确认 → 整改闭环
Create template → Generate evaluation → Multi-dimensional scoring → Auto-calculate total → Auto-grade A/B/C/D → Supplier confirm → Rectification loop closure
```

### 黑名单拦截机制 / Blacklist Interception Mechanism

采购/招标/询价模块在执行操作前会调用 `SupplierRiskService.isBlacklisted(supplierId)` 前置校验，已拉黑的供应商拒绝继续操作。 / Purchase, bidding, and inquiry modules call isBlacklisted() as a pre-check before operations.

### 物料多单位换算 / Multi-Unit Material Conversion

`material_unit` 表存储基本单位与采购/库存/发料单位之间的换算率，用于采购下单和库存计算时的数量转换。 / The material_unit table stores conversion rates between base unit and purchase/inventory/issue units for quantity conversion.
