package com.atlas.workflow.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("WorkflowService 工作流服务测试")
@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private TaskService taskService;

    private WorkflowService workflowService;

    @BeforeEach
    void setUp() {
        workflowService = new WorkflowService(runtimeService, taskService);
    }

    // ==================== 流程启动 ====================

    @Test
    @DisplayName("启动流程应返回 ProcessInstance")
    void should_start_process_and_return_instance() {
        ProcessInstance mockPi = mock(ProcessInstance.class);
        when(mockPi.getId()).thenReturn("proc-001");
        when(runtimeService.startProcessInstanceByKey(anyString(), anyString(), anyMap()))
                .thenReturn(mockPi);

        ProcessInstance result = workflowService.startProcess("purchase-approval", "PO-100",
                new HashMap<>());

        assertThat(result.getId()).isEqualTo("proc-001");
        verify(runtimeService).startProcessInstanceByKey("purchase-approval", "PO-100", new HashMap<>());
    }

    @Test
    @DisplayName("启动流程应传递 variables")
    void should_pass_variables_when_start_process() {
        ProcessInstance mockPi = mock(ProcessInstance.class);
        Map<String, Object> vars = new HashMap<>();
        vars.put("amount", 50000L);
        vars.put("deptId", 3L);
        when(runtimeService.startProcessInstanceByKey(anyString(), anyString(), eq(vars)))
                .thenReturn(mockPi);

        workflowService.startProcess("purchase-approval", "PO-100", vars);

        verify(runtimeService).startProcessInstanceByKey("purchase-approval", "PO-100", vars);
    }

    // ==================== 待办查询 ====================

    @Test
    @DisplayName("应返回指定用户的待办任务列表")
    void should_return_pending_tasks_for_assignee() {
        TaskQuery taskQuery = mock(TaskQuery.class);
        Task mockTask = mock(Task.class);
        when(mockTask.getId()).thenReturn("task-001");
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskAssignee("user1")).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(Collections.singletonList(mockTask));

        List<Task> tasks = workflowService.getPendingTasks("user1");

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getId()).isEqualTo("task-001");
    }

    @Test
    @DisplayName("无待办时应返回空列表")
    void should_return_empty_list_when_no_pending_tasks() {
        TaskQuery taskQuery = mock(TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskAssignee("user1")).thenReturn(taskQuery);
        when(taskQuery.active()).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(Collections.emptyList());

        List<Task> tasks = workflowService.getPendingTasks("user1");

        assertThat(tasks).isEmpty();
    }

    // ==================== 审批通过 ====================

    @Test
    @DisplayName("审批通过应完成任务并记录 comment")
    void should_complete_task_when_approve() {
        Task mockTask = mock(Task.class);
        when(mockTask.getProcessInstanceId()).thenReturn("pi-001");
        TaskQuery taskQuery = mock(TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("task-001")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask);

        workflowService.approve("task-001", "同意采购");

        verify(taskService).addComment(eq("task-001"), eq("pi-001"), eq("APPROVED"), eq("同意采购"));
        verify(taskService).complete(eq("task-001"), argThat(vars ->
                Boolean.TRUE.equals(vars.get("approved")) && "同意采购".equals(vars.get("comment"))));
    }

    @Test
    @DisplayName("审批时任务不存在应抛 BizException")
    void should_throw_when_task_not_found_on_approve() {
        TaskQuery taskQuery = mock(TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("task-999")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(null);

        assertThatThrownBy(() -> workflowService.approve("task-999", "同意"))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.WORKFLOW_TASK_NOT_FOUND.getCode());
    }

    // ==================== 驳回 ====================

    @Test
    @DisplayName("驳回应设置 approved=false 并记录驳回原因")
    void should_reject_task_with_reason() {
        Task mockTask = mock(Task.class);
        when(mockTask.getProcessInstanceId()).thenReturn("pi-002");
        TaskQuery taskQuery = mock(TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("task-002")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(mockTask);

        workflowService.reject("task-002", "金额超出预算");

        verify(taskService).addComment(eq("task-002"), eq("pi-002"), eq("REJECTED"), eq("金额超出预算"));
        verify(taskService).complete(eq("task-002"), argThat(vars ->
                Boolean.FALSE.equals(vars.get("approved")) &&
                "金额超出预算".equals(vars.get("rejectReason"))));
    }

    @Test
    @DisplayName("驳回时任务不存在应抛 BizException")
    void should_throw_when_task_not_found_on_reject() {
        TaskQuery taskQuery = mock(TaskQuery.class);
        when(taskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("task-999")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(null);

        assertThatThrownBy(() -> workflowService.reject("task-999", "原因"))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.WORKFLOW_TASK_NOT_FOUND.getCode());
    }

    // ==================== 流程实例查询 ====================

    @Test
    @DisplayName("应返回流程实例")
    void should_return_process_instance() {
        ProcessInstanceQuery piQuery = mock(ProcessInstanceQuery.class);
        ProcessInstance mockPi = mock(ProcessInstance.class);
        when(mockPi.getId()).thenReturn("pi-001");
        when(runtimeService.createProcessInstanceQuery()).thenReturn(piQuery);
        when(piQuery.processInstanceId("pi-001")).thenReturn(piQuery);
        when(piQuery.singleResult()).thenReturn(mockPi);

        ProcessInstance result = workflowService.getProcessInstance("pi-001");

        assertThat(result.getId()).isEqualTo("pi-001");
    }

    @Test
    @DisplayName("流程实例不存在应抛 BizException")
    void should_throw_when_process_not_found() {
        ProcessInstanceQuery piQuery = mock(ProcessInstanceQuery.class);
        when(runtimeService.createProcessInstanceQuery()).thenReturn(piQuery);
        when(piQuery.processInstanceId("pi-999")).thenReturn(piQuery);
        when(piQuery.singleResult()).thenReturn(null);

        assertThatThrownBy(() -> workflowService.getProcessInstance("pi-999"))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.WORKFLOW_NOT_EXIST.getCode());
    }
}
