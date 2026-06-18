package com.atlas.workflow.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.workflow.model.TaskInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 审批工作流核心业务服务 / Approval workflow core business service
 * <p>
 * 基于 Flowable 7.0 API，封装流程实例的启动、任务完成、待办查询和审批历史追溯。
 * 流程 XML 定义文件需放置在 resources/processes/ 目录下。 /
 * Based on Flowable 7.0 API, encapsulates process instance startup, task completion,
 * pending task queries, and approval history tracing.
 * Process XML definition files should be placed under resources/processes/.
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;

    /**
     * 启动流程实例 / Start a process instance
     * <p>
     * 根据流程定义 Key 启动一个新流程，并将 businessKey 绑定到业务单据。 /
     * Starts a new process by process definition key and binds businessKey to the business document.
     *
     * @param processKey  流程定义 Key（对应 .bpmn20.xml 中的 process id） / Process definition key
     * @param businessKey 业务单据标识（如采购订单号 orderNo） / Business document identifier (e.g. orderNo)
     * @param variables   流程变量（如审批金额、申请人等） / Process variables (e.g. approval amount, applicant)
     * @return 启动的流程实例信息 / Started process instance info
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startProcess(String processKey, String businessKey, Map<String, Object> variables) {
        // 校验流程定义是否存在 / Validate process definition exists
        long count = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .latestVersion()
                .count();
        if (count == 0) {
            throw new BizException(ErrorCode.WORKFLOW_NOT_EXIST, "流程定义不存在: " + processKey);
        }

        // 启动流程实例 / Start process instance
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                processKey, businessKey, variables != null ? variables : new HashMap<>());

        log.info("流程启动成功: processKey={} businessKey={} processInstanceId={}",
                processKey, businessKey, processInstance.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("processInstanceId", processInstance.getId());
        result.put("businessKey", processInstance.getBusinessKey());
        result.put("processDefinitionId", processInstance.getProcessDefinitionId());
        result.put("isEnded", processInstance.isEnded());
        return result;
    }

    /**
     * 完成审批任务（审批通过 / 驳回） / Complete an approval task (approve / reject)
     * <p>
     * 审批人完成任务，通过 variables 中的 approved 字段决定审批结果。 /
     * Approver completes the task, result determined by approved field in variables.
     *
     * @param taskId    任务ID / Task ID
     * @param variables 流程变量（至少包含 approved: true/false，可选 comment 审批意见） / Process variables
     * @return 完成后的任务信息 / Completed task info
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> completeTask(String taskId, Map<String, Object> variables) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BizException(ErrorCode.WORKFLOW_TASK_NOT_FOUND);
        }

        // 补充默认变量 / Fill default variables
        if (variables == null) {
            variables = new HashMap<>();
        }
        if (!variables.containsKey("approved")) {
            variables.put("approved", true);
        }

        // 记录审批人信息 / Record approver info
        variables.put("approver", task.getAssignee());
        variables.put("approvalTime", new Date());

        // 完成任务 / Complete task
        taskService.complete(taskId, variables);

        log.info("任务完成: taskId={} taskName={} assignee={} approved={}",
                taskId, task.getName(), task.getAssignee(), variables.get("approved"));

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", taskId);
        result.put("taskName", task.getName());
        result.put("processInstanceId", task.getProcessInstanceId());
        result.put("completed", true);
        return result;
    }

    /**
     * 查询某用户的待办任务列表 / Query pending tasks for a user
     *
     * @param assignee 处理人标识 / Assignee identifier
     * @return 待办任务列表 / Pending task list
     */
    public List<TaskInfo> getPendingTasks(String assignee) {
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(assignee)
                .orderByTaskCreateTime().desc()
                .list();

        return tasks.stream().map(task -> {
            Map<String, Object> processVariables = taskService.getVariables(task.getId());
            ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId()).singleResult();
            String businessKey = pi != null ? pi.getBusinessKey() : null;
            return TaskInfo.builder()
                    .taskId(task.getId())
                    .taskName(task.getName())
                    .processInstanceId(task.getProcessInstanceId())
                    .businessKey(businessKey)
                    .assignee(task.getAssignee())
                    .createTime(toLocalDateTime(task.getCreateTime()))
                    .processVariables(processVariables)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 分页查询待办任务 / Paginated query of pending tasks
     *
     * @param assignee 处理人 / Assignee
     * @param page     当前页（0-based） / Current page (0-based)
     * @param size     每页大小 / Page size
     * @return 待办任务分页结果 / Paginated result
     */
    public Map<String, Object> getPendingTasksPaged(String assignee, int page, int size) {
        TaskQuery query = taskService.createTaskQuery()
                .taskAssignee(assignee)
                .orderByTaskCreateTime().desc();

        long total = query.count();
        List<Task> tasks = query.listPage(page * size, size);

        List<TaskInfo> taskInfos = tasks.stream().map(task -> {
            Map<String, Object> processVariables = taskService.getVariables(task.getId());
            ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId()).singleResult();
            String businessKey = pi != null ? pi.getBusinessKey() : null;
            return TaskInfo.builder()
                    .taskId(task.getId())
                    .taskName(task.getName())
                    .processInstanceId(task.getProcessInstanceId())
                    .businessKey(businessKey)
                    .assignee(task.getAssignee())
                    .createTime(toLocalDateTime(task.getCreateTime()))
                    .processVariables(processVariables)
                    .build();
        }).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("records", taskInfos);
        return result;
    }

    /**
     * 查询某业务单据的审批历史（已完成的审批节点和审批人） /
     * Query approval history for a business document (completed nodes and approvers)
     *
     * @param businessKey 业务单据标识 / Business document identifier
     * @return 审批历史节点列表 / Approval history node list
     */
    public List<Map<String, Object>> getProcessHistory(String businessKey) {
        // 通过 businessKey 查找历史流程实例 / Find historic process instance by businessKey
        List<HistoricActivityInstance> activities = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(getProcessInstanceIdByBusinessKey(businessKey))
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();

        return activities.stream()
                .filter(act -> "userTask".equals(act.getActivityType())) // 仅保留用户任务节点 / Only user tasks
                .map(act -> {
                    Map<String, Object> node = new LinkedHashMap<>();
                    node.put("activityId", act.getActivityId());
                    node.put("activityName", act.getActivityName());
                    node.put("activityType", act.getActivityType());
                    node.put("assignee", act.getAssignee());
                    node.put("startTime", toLocalDateTime(act.getStartTime()));
                    node.put("endTime", toLocalDateTime(act.getEndTime()));
                    node.put("durationInMillis", act.getDurationInMillis());
                    return node;
                })
                .collect(Collectors.toList());
    }

    /**
     * 查询某业务单据的当前活跃任务 / Query active tasks for a business document
     *
     * @param businessKey 业务单据标识 / Business document identifier
     * @return 当前任务列表 / Current task list
     */
    public List<TaskInfo> getActiveTasksByBusinessKey(String businessKey) {
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceBusinessKey(businessKey)
                .orderByTaskCreateTime().desc()
                .list();

        return tasks.stream().map(task -> {
            Map<String, Object> processVariables = taskService.getVariables(task.getId());
            ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId()).singleResult();
            String bk = pi != null ? pi.getBusinessKey() : null;
            return TaskInfo.builder()
                    .taskId(task.getId())
                    .taskName(task.getName())
                    .processInstanceId(task.getProcessInstanceId())
                    .businessKey(bk)
                    .assignee(task.getAssignee())
                    .createTime(toLocalDateTime(task.getCreateTime()))
                    .processVariables(processVariables)
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 根据 businessKey 获取流程实例ID / Get process instance ID by businessKey
     */
    private String getProcessInstanceIdByBusinessKey(String businessKey) {
        // 先从运行时查找 / Search runtime first
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey)
                .singleResult();
        if (pi != null) {
            return pi.getId();
        }

        // 再从历史中查找 / Then search history
        org.flowable.engine.history.HistoricProcessInstance hpi = historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey)
                .singleResult();
        if (hpi != null) {
            return hpi.getId();
        }

        throw new BizException(ErrorCode.WORKFLOW_NOT_EXIST, "未找到业务单据对应的流程实例: " + businessKey);
    }

    /**
     * Date → LocalDateTime 转换 / Date to LocalDateTime conversion
     */
    private LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
