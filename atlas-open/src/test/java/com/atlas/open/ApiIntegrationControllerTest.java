package com.atlas.open.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("ApiIntegrationController 集成调用入口测试")
@WebMvcTest(ApiIntegrationController.class)
class ApiIntegrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("POST /api/open/integration/webhook/{channel} 应接收 webhook")
    void should_receive_webhook() throws Exception {
        mockMvc.perform(post("/api/open/integration/webhook/erp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"event\":\"order.created\",\"data\":{\"id\":\"PO-100\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("webhook received from erp"));
    }

    @Test
    @DisplayName("GET /api/open/integration/stats 应返回调用统计")
    void should_return_stats() throws Exception {
        mockMvc.perform(get("/api/open/integration/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalCalls").value(12580))
                .andExpect(jsonPath("$.data.successRate").value("99.2%"))
                .andExpect(jsonPath("$.data.avgLatency").value(45));
    }
}
