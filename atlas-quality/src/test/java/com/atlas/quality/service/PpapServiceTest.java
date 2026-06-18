package com.atlas.quality.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.quality.entity.PpapElement;
import com.atlas.quality.entity.PpapSubmission;
import com.atlas.quality.mapper.PpapElementMapper;
import com.atlas.quality.mapper.PpapSubmissionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PPAP 服务单元测试 / PPAP service unit tests
 *
 * @author Atlas Team
 * @since 1.2.24
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("PPAP服务单元测试")
class PpapServiceTest {

    @Mock
    private PpapSubmissionMapper submissionMapper;
    @Mock
    private PpapElementMapper elementMapper;

    @InjectMocks
    private PpapService ppapService;

    // ======================== 创建 PPAP 提交 ========================

    @Test
    @Order(1)
    @DisplayName("创建 PPAP 提交（Level 3）：自动生成 14 个必选要素 + 4 个可选要素")
    void testCreateSubmission_Level3() {
        PpapSubmission submission = buildSubmission(1L, 3); // Level 3 = 14 required elements

        when(submissionMapper.insert(submission)).thenReturn(1);
        when(elementMapper.insert(any(PpapElement.class))).thenReturn(1);

        PpapSubmission result = ppapService.createSubmission(submission);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PpapSubmission.STATUS_PENDING);

        // 验证插入了 18 个要素 / Verify 18 elements inserted
        ArgumentCaptor<PpapElement> captor = ArgumentCaptor.forClass(PpapElement.class);
        verify(elementMapper, times(18)).insert(captor.capture());

        // Level 3 应有 14 个必选要素 / Level 3 should have 14 required elements
        long requiredCount = captor.getAllValues().stream()
                .filter(e -> e.getIsRequired() == 1)
                .count();
        assertThat(requiredCount).isEqualTo(14);

        verify(submissionMapper).insert(submission);
    }

    @Test
    @Order(2)
    @DisplayName("供应商提交 PPAP 要素文件：更新 submitted 状态和路径")
    void testSubmitElement() {
        PpapSubmission submission = new PpapSubmission();
        submission.setId(1L);

        PpapElement element = new PpapElement();
        element.setId(10L);
        element.setSubmissionId(1L);
        element.setElementCode("CONTROL_PLAN");
        element.setSubmitted(0);
        element.setFilePath(null);

        when(submissionMapper.selectById(1L)).thenReturn(submission);
        when(elementMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(element);
        when(elementMapper.updateById(element)).thenReturn(1);

        PpapElement result = ppapService.submitElement(1L, "CONTROL_PLAN",
                "/upload/ppap/1/CONTROL_PLAN.pdf");

        assertThat(result).isNotNull();
        assertThat(result.getSubmitted()).isEqualTo(1);
        assertThat(result.getFilePath()).isEqualTo("/upload/ppap/1/CONTROL_PLAN.pdf");
        verify(submissionMapper).selectById(1L);
        verify(elementMapper).updateById(element);
    }

    @Test
    @Order(3)
    @DisplayName("提交不存在的 PPAP 记录：抛 BizException(DATA_NOT_EXIST)")
    void testSubmitElement_SubmissionNotFound() {
        when(submissionMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> ppapService.submitElement(999L, "PSW", "/path/to/file.pdf"))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("PPAP提交记录不存在");

        verify(submissionMapper).selectById(999L);
        verify(elementMapper, never()).selectOne(any());
    }

    // ======================== 辅助方法 ========================

    private PpapSubmission buildSubmission(Long supplierId, int ppapLevel) {
        PpapSubmission s = new PpapSubmission();
        s.setSupplierId(supplierId);
        s.setMaterialId(200L);
        s.setMaterialName("制动盘总成 A/Brake Disc Assembly A");
        s.setPartNo("BP-8840-001");
        s.setPpapLevel(ppapLevel);
        s.setSubmittedBy("supplier-admin");
        return s;
    }
}