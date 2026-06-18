package com.atlas.workflow.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.workflow.model.TaskInfo;
import com.atlas.workflow.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 审批工作流 REST API / Approval workflow REST API
 * <p>
 * 基于 Flowable 7.0，提供流程启动、任务审批、待办查询和审批历史追溯。 /
 * Based on Flowable 7.0, provides process startup, task approval, pending task queries, and approval history.
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;

    /**
     * 启动流程实例 / Start a process instance
     *
     * @param processKey  流程定义 Key / Process definition key
     * @param businessKey 业务单据标识 / Business document identifier
     * @param variables   流程变量 / Process variables
     */
    @PostMapping("/process/start")
    @RequirePermission("workflow:start")
    public Result<Map<String, Object>> startProcess(
            @RequestParam String processKey,
            @RequestParam String businessKey,
            @RequestBody(required = false) Map<String, Object> variables) {
        return Result.ok(workflowService.startProcess(processKey, businessKey, variables));
    }

    /**
     * 完成审批任务 / Complete approval task
     *
     * @param taskId    任务ID / Task ID
     * @param variables 审批变量（approved: true/false, comment: 审批意见） / Approval variables
     */
    @PostMapping("/task/{taskId}/complete")
    @RequirePermission("workflow:approve")
    public Result<Map<String, Object>> completeTask(
            @PathVariable String taskId,
            @RequestBody(required = false) Map<String, Object> variables) {
        return Result.ok(workflowService.completeTask(taskId, variables));
    }

    /**
     * 查询待办任务列表 / Query pending task list
     *
     * @param assignee 处理人 / Assignee
     */
    @GetMapping("/task/pending")
    @RequirePermission("workflow:view")
    public Result<List<TaskInfo>> getPendingTasks(@RequestParam String assignee) {
        return Result.ok(workflowService.getPendingTasks(assignee));
    }

    /**
     * 分页查询待办任务 / Paginated query of pending tasks
     *
     * @param assignee 处理人 / Assignee
     * @param page     当前页（0-based） / Current page (0-based)
     * @param size     每页大小 / Page size
     */
    @GetMapping("/task/pending/page")
    @RequirePermission("workflow:view")
    @SuppressWarnings("unchecked")
    public Result<PageResult<TaskInfo>> getPendingTasksPaged(
            @RequestParam String assignee,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = workflowService.getPendingTasksPaged(assignee, page, size);
        return PageResult.ok(
                (Long) result.get("total"),
                (Integer) result.get("page"),
                (Integer) result.get("size"),
                (List<TaskInfo>) result.get("records"));
    }

    /**
     * 查询业务单据的审批历史 / Query approval history for a business document
     *
     * @param businessKey 业务单据标识 / Business document identifier
     */
    @GetMapping("/history/{businessKey}")
    @RequirePermission("workflow:view")
    public Result<List<Map<String, Object>>> getProcessHistory(@PathVariable String businessKey) {
        return Result.ok(workflowService.getProcessHistory(businessKey));
    }

    /**
     * 查询业务单据的当前活跃任务 / Query active tasks for a business document
     *
     * @param businessKey 业务单据标识 / Business document identifier
     */
    @GetMapping("/task/active/{businessKey}")
    @RequirePermission("workflow:view")
    public Result<List<TaskInfo>> getActiveTasks(@PathVariable String businessKey) {
        return Result.ok(workflowService.getActiveTasksByBusinessKey(businessKey));
    }
}
