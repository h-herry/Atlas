# atlas-workflow — 工作流引擎 / Workflow Engine

## 功能概述 / Overview

`atlas-workflow` 模块集成 Flowable 7.0 工作流引擎，为系统的审批场景（合同审批、采购审批等）提供流程定义、审批流转、待办查询和审批历史追溯能力。

---

The `atlas-workflow` module integrates the Flowable 7.0 workflow engine, providing process definition, approval flow, pending task queries, and approval history tracing for the system's approval scenarios (contract approval, purchase approval, etc.).

**端口 / Port**：8087 | **数据库 / Database**：atlas_flowable | **文件数 / File count**：4 Java classes

---

## 数据库表列表 / Database Tables

| 表名 / Table | 说明 / Description | 迁移 / Migration |
|------|------|------|
| biz_workflow | 业务工作流关联表（业务类型 + 业务ID → 流程实例） / Business-workflow association (biz type + biz ID → process instance) | V7 |

> Flowable 内置 70+ 张 `ACT_*` 表由引擎自动创建，无需手动维护。 / Flowable's built-in 70+ ACT_* tables are auto-created by the engine.

---

## Controller API 列表 / Controller API List

### WorkflowController（/api/workflow）

| 端点 / Endpoint | 方法 / Method | 权限 / Permission | 说明 / Description |
|------|------|------|------|
| `/api/workflow/process/start` | POST | workflow:manage | 启动流程 / Start process |
| `/api/workflow/task/complete` | POST | workflow:manage | 完成审批任务 / Complete approval task |
| `/api/workflow/tasks/pending` | GET | workflow:view | 查询待办任务 / Query pending tasks |
| `/api/workflow/process/history` | GET | workflow:view | 查询流程审批历史 / Query process history |
| `/api/workflow/tasks/active` | GET | workflow:view | 查询活动任务 / Query active tasks |

---

## 核心 Service / Core Service

### WorkflowService

- `startProcess(bizType, bizId, variables)`：启动流程实例 → 关联 biz_workflow 表 / Start process instance → associate biz_workflow
- `completeTask(taskId, approved, comment, operatorId, operatorName)`：审批通过/驳回 / Approve or reject
- `getPendingTasks(operatorId)`：查询当前用户的待办任务 / Query current user's pending tasks
- `getProcessHistory(bizType, bizId)`：查询该业务流程的审批历史 / Query business process approval history
- `getActiveTasks(bizType, bizId)`：查询当前活跃任务节点 / Query current active task nodes

---

## 技术要点 / Technical Details

### 业务-流程关联 / Business-Process Association

`biz_workflow` 表建立业务数据与 Flowable 流程实例的映射 / Maps business data to Flowable process instances：

| 字段 / Field | 说明 / Description |
|------|------|
| biz_type | 业务类型：CONTRACT（合同）/ PURCHASE（采购）等 / Business type: CONTRACT, PURCHASE, etc. |
| biz_id | 业务主键 ID / Business primary key |
| process_instance_id | Flowable 流程实例 ID / Flowable process instance ID |
| status | 流程状态 / Process status |

### Flowable 集成方式 / Flowable Integration

- 流程定义 / Process definition：BPMN 2.0 XML 文件（`resources/processes/`）
- 流程启动 / Process start：`RuntimeService.startProcessInstanceByKey()`
- 任务处理 / Task handling：`TaskService.complete()`
- 审批历史 / Approval history：`HistoryService.createHistoricProcessInstanceQuery()`
