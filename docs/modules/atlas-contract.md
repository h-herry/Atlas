# atlas-contract — 合同管理 / Contract Management

## 功能概述 / Overview

`atlas-contract` 模块负责合同全生命周期管理，基于状态机模式实现从草稿到完成的 10 个状态完整流转。支持审批流对接（Flowable 7.0）、变更日志追溯（before/after JSON 快照）和驳回重提机制。

---

The `atlas-contract` module handles the full contract lifecycle, implementing complete 10-state transitions from draft to completion based on a state machine pattern. It supports approval workflow integration (Flowable 7.0), change log tracing (before/after JSON snapshots), and rejection re-submission mechanism.

**端口 / Port**：8083 | **数据库 / Database**：atlas_contract | **文件数 / File count**：8 Java classes

---

## 数据库表列表 / Database Tables（3 张 / 3 tables）

| 表名 / Table | 说明 / Description | 迁移 / Migration |
|------|------|------|
| contract | 合同主表（10 状态 + status 字段） / Contract master (10 states + status field) | V3 |
| contract_change_log | 合同变更日志（before/after JSON 快照） / Contract change log (before/after JSON snapshot) | V3 |
| contract_approval | 合同审批记录表 / Contract approval record | V3 |

---

## Controller API 列表 / Controller API List

### ContractController（/api/contract）

| 端点 / Endpoint | 方法 | 权限 / Permission | 说明 / Description |
|------|------|------|------|
| `/api/contract/page` | GET | contract:view | 分页查询合同列表 / Paginated contract query |
| `/api/contract/{id}` | GET | contract:view | 查询合同详情（含明细） / Query contract detail (with items) |
| `/api/contract` | POST | contract:manage | 新增合同（状态 = DRAFT） / Create contract (status = DRAFT) |
| `/api/contract` | PUT | contract:manage | 编辑合同（仅 DRAFT/REJECTED 状态） / Edit contract (DRAFT/REJECTED only) |
| `/api/contract/{id}/transition` | PUT | contract:manage | 状态流转（含 allowTargets 校验） / State transition (with allowTargets validation) |
| `/api/contract/{id}/reject` | PUT | contract:approve | 驳回合同（含驳回原因） / Reject contract (with reason) |

---

## 合同状态机 / Contract State Machine

### 枚举定义 / Enum Definition（ContractStatusEnum）

| Code | 枚举值 / Enum | 显示名 / Display | 类型 / Type | allowTargets |
|------|--------|--------|------|-------------|
| 0 | DRAFT | 草稿 / Draft | 编辑态 / Editable | SUBMITTED |
| 1 | SUBMITTED | 已提交 / Submitted | 流程态 / In-process | REVIEWING |
| 2 | REVIEWING | 审核中 / Under Review | 流程态 / In-process | APPROVED, REJECTED |
| 3 | APPROVED | 审核通过 / Approved | 流程态 / In-process | SIGNED |
| 4 | REJECTED | 驳回 / Rejected | 编辑态 / Editable | SUBMITTED, DRAFT |
| 5 | SIGNED | 已签署 / Signed | 执行态 / Executing | EXECUTING |
| 6 | EXECUTING | 执行中 / In Execution | 执行态 / Executing | CHANGING, TERMINATED, COMPLETED |
| 7 | CHANGING | 变更中 / Changing | 流程态 / In-process | EXECUTING |
| 8 | TERMINATED | 已终止 / Terminated | 终态 / Terminal | — |
| 9 | COMPLETED | 已完成 / Completed | 终态 / Terminal | — |

---

## 核心 Service / Core Service

### ContractService

- `transition(contractId, targetStatus)`：校验 `validateTransition(from, to)` → 更新状态 → 记录变更日志 / Validate transition → update status → record change log
- `reject(contractId, reason)`：状态 → REJECTED → 记录审批记录 / Set status to REJECTED → record approval entry
- 每次状态变更写入 `contract_change_log`（before/after JSON 快照） / Each status change writes to contract_change_log (before/after JSON snapshot)

---

## 技术要点 / Technical Details

### 状态机入口守卫 / State Machine Entry Guard

```java
public static void validateTransition(int fromCode, int toCode) {
    ContractStatusEnum from = fromCode(fromCode);
    if (!from.getAllowTargets().contains(toCode)) {
        throw new BizException(ErrorCode.CONTRACT_CANNOT_MODIFY);
    }
}
```

- 每个状态枚举值内置 `allowTargets`（合法目标状态集合） / Each enum value contains allowTargets (set of valid target states)
- `validateTransition()` 在 Service 层调用，校验失败抛 `BizException` / Called at Service layer; throws BizException on validation failure
- 8（已终止）和 9（已完成）为终态，不允许再流转 / States 8 (Terminated) and 9 (Completed) are terminal; no further transitions allowed

### 变更日志追溯 / Change Log Tracing

`contract_change_log` 表每次状态变更记录 / Records per state change：
- `from_status` / `to_status`：变更前后状态 / Before/after status
- `before_data` / `after_data`：变更前后数据 JSON 快照 / Before/after data JSON snapshot
- `change_reason`：变更原因（驳回时记录驳回原因） / Change reason (records rejection reason)
- `operator` / `operate_time`：操作人与时间 / Operator and timestamp

### 审批流对接 / Approval Workflow Integration

- 合同提交审批时对接 Flowable 工作流引擎 / Integrates with Flowable workflow engine on submission
- `contract_approval` 表记录审批节点历史 / contract_approval table records approval node history
