package com.atlas.workflow.controller;

import com.atlas.workflow.service.WorkflowService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("WorkflowController API 端点测试")
@WebMvcTest(WorkflowController.class)
class WorkflowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkflowService workflowService;

    @Test
    @DisplayName("POST /api/workflow/process/start 应返回流程实例 ID")
    void should_start_process_and_return_id() throws Exception {
        ProcessInstance mockPi = mock(ProcessInstance.class);
        when(mockPi.getId()).thenReturn("pi-001");
        when(workflowService.startProcess(anyString(), anyString(), anyMap()))
                .thenReturn(mockPi);

        mockMvc.perform(post("/api/workflow/process/start")
                        .param("definitionKey", "purchase-approval")
                        .param("businessKey", "PO-100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("pi-001"));
    }

    @Test
    @DisplayName("GET /api/workflow/tasks/pending 应返回待办列表")
    void should_return_pending_tasks() throws Exception {
        Task mockTask = mock(Task.class);
        when(mockTask.getId()).thenReturn("task-001");
        when(mockTask.getName()).thenReturn("审批采购订单");
        when(workflowService.getPendingTasks("user1"))
                .thenReturn(Collections.singletonList(mockTask));

        mockMvc.perform(get("/api/workflow/tasks/pending")
                        .param("assignee", "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value("task-001"));
    }

    @Test
    @DisplayName("POST /api/workflow/tasks/{taskId}/approve 应审批通过")
    void should_approve_task() throws Exception {
        doNothing().when(workflowService).approve(anyString(), anyString());

        mockMvc.perform(post("/api/workflow/tasks/task-001/approve")
                        .param("comment", "同意"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(workflowService).approve("task-001", "同意");
    }

    @Test
    @DisplayName("POST /api/workflow/tasks/{taskId}/reject 应驳回")
    void should_reject_task() throws Exception {
        doNothing().when(workflowService).reject(anyString(), anyString());

        mockMvc.perform(post("/api/workflow/tasks/task-002/reject")
                        .param("reason", "金额超出预算"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(workflowService).reject("task-002", "金额超出预算");
    }

    @Test
    @DisplayName("GET /api/workflow/process/{id} 应返回流程实例")
    void should_get_process_instance() throws Exception {
        ProcessInstance mockPi = mock(ProcessInstance.class);
        when(mockPi.getId()).thenReturn("pi-001");
        when(workflowService.getProcessInstance("pi-001")).thenReturn(mockPi);

        mockMvc.perform(get("/api/workflow/process/pi-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("pi-001"));
    }

    @Test
    @DisplayName("审批不提供 comment 时应使用默认值")
    void should_use_default_comment_when_not_provided() throws Exception {
        doNothing().when(workflowService).approve(anyString(), anyString());

        mockMvc.perform(post("/api/workflow/tasks/task-003/approve"))
                .andExpect(status().isOk());

        verify(workflowService).approve("task-003", "同意");
    }
}
