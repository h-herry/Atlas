package com.atlas.open.controller;

import com.atlas.open.service.DynamicApiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("ApiConfigController API 配置端点测试")
@WebMvcTest(ApiConfigController.class)
class ApiConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DynamicApiService dynamicApiService;

    @Test
    @DisplayName("POST /api/open/config/execute 应执行 API 调用")
    void should_execute_api_call() throws Exception {
        when(dynamicApiService.executeApiCall(anyString(), anyMap(), anyString(), anyMap()))
                .thenReturn("{\"status\":\"ok\"}");

        mockMvc.perform(post("/api/open/config/execute")
                        .param("authType", "BEARER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"url":"http://api.test","credentials":{"token":"jwt"},
                                "variables":{"id":"123"}}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("{\"status\":\"ok\"}"));
    }

    @Test
    @DisplayName("GET /api/open/config/auth-types 应返回鉴权类型列表")
    void should_return_auth_types() throws Exception {
        when(dynamicApiService.getSupportedAuthTypes())
                .thenReturn(new String[]{"BASIC", "BEARER", "API_KEY", "OAUTH2", "HMAC"});

        mockMvc.perform(get("/api/open/config/auth-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0]").value("BASIC"))
                .andExpect(jsonPath("$.data[4]").value("HMAC"));
    }
}
