package com.atlas.receipt.service;

import com.atlas.common.mq.producer.AbstractMessageProducer;
import com.atlas.receipt.entity.ReceiptOutbox;
import com.atlas.receipt.mapper.ReceiptOutboxMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("收货本地消息表补偿服务单元测试")
class ReceiptOutboxServiceTest {

    @Mock private ReceiptOutboxMapper outboxMapper;
    @Mock private ReceiptService.ReceiptConfirmProducer receiptConfirmProducer;

    @InjectMocks
    private ReceiptOutboxService outboxService;

    // ======================== MQ 发送失败写入 outbox ========================

    @Test
    @DisplayName("MQ 发送失败写入 outbox 表：定时任务扫描到待发送消息并补发成功")
    void testOutboxInsert() {
        ReceiptOutbox pending = buildOutbox(1L, 101L, "{\"receiptId\":101}", 0, 0,
                LocalDateTime.now().minusMinutes(2));

        when(outboxMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(pending));
        doNothing().when(receiptConfirmProducer).sendAsync(anyString());
        when(outboxMapper.updateById(any(ReceiptOutbox.class))).thenReturn(1);

        outboxService.retryPendingMessages();

        // 验证消息发送成功
        verify(receiptConfirmProducer).sendAsync("{\"receiptId\":101}");

        ArgumentCaptor<ReceiptOutbox> captor = ArgumentCaptor.forClass(ReceiptOutbox.class);
        verify(outboxMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(1); // 已发送
    }

    // ======================== 重试发送 ========================

    @Test
    @DisplayName("定时任务重试发送成功：失败 1 次后重试成功")
    void testOutboxRetry() {
        ReceiptOutbox pending = buildOutbox(2L, 202L, "{\"receiptId\":202}", 0, 1,
                LocalDateTime.now().minusMinutes(1));

        when(outboxMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(pending));
        doNothing().when(receiptConfirmProducer).sendAsync(anyString());
        when(outboxMapper.updateById(any(ReceiptOutbox.class))).thenReturn(1);

        outboxService.retryPendingMessages();

        ArgumentCaptor<ReceiptOutbox> captor = ArgumentCaptor.forClass(ReceiptOutbox.class);
        verify(outboxMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(1); // 已发送
    }

    // ======================== 超过最大重试次数 ========================

    @Test
    @DisplayName("超过 5 次重试标记失败：retryCount >= 5，status 置为 2")
    void testOutboxMaxRetryExceeded() {
        ReceiptOutbox pending = buildOutbox(3L, 303L, "{\"receiptId\":303}", 0, 4,
                LocalDateTime.now().minusMinutes(1));

        when(outboxMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(pending));
        doThrow(new RuntimeException("MQ broker unavailable")).when(receiptConfirmProducer).sendAsync(anyString());
        when(outboxMapper.updateById(any(ReceiptOutbox.class))).thenReturn(1);

        outboxService.retryPendingMessages();

        ArgumentCaptor<ReceiptOutbox> captor = ArgumentCaptor.forClass(ReceiptOutbox.class);
        verify(outboxMapper).updateById(captor.capture());

        ReceiptOutbox updated = captor.getValue();
        assertThat(updated.getRetryCount()).isEqualTo(5);
        assertThat(updated.getStatus()).isEqualTo(2); // 发送失败
        assertThat(updated.getErrorMsg()).contains("MQ broker unavailable");
    }

    @Test
    @DisplayName("重试次数未超上限：更新重试时间和计数，保持 status=0")
    void testOutboxRetry_IncrementCount() {
        ReceiptOutbox pending = buildOutbox(4L, 404L, "{\"receiptId\":404}", 0, 2,
                LocalDateTime.now().minusMinutes(1));

        when(outboxMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(pending));
        doThrow(new RuntimeException("Network timeout")).when(receiptConfirmProducer).sendAsync(anyString());
        when(outboxMapper.updateById(any(ReceiptOutbox.class))).thenReturn(1);

        outboxService.retryPendingMessages();

        ArgumentCaptor<ReceiptOutbox> captor = ArgumentCaptor.forClass(ReceiptOutbox.class);
        verify(outboxMapper).updateById(captor.capture());

        ReceiptOutbox updated = captor.getValue();
        assertThat(updated.getStatus()).isEqualTo(0); // 仍然是待发送
        assertThat(updated.getRetryCount()).isEqualTo(3); // 2 + 1
        assertThat(updated.getNextRetryTime()).isAfter(LocalDateTime.now());
    }

    // ======================== 无待发送消息 ========================

    @Test
    @DisplayName("无待发送消息：定时任务空转")
    void testRetry_NoPendingMessages() {
        when(outboxMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        outboxService.retryPendingMessages();

        verify(receiptConfirmProducer, never()).sendAsync(anyString());
        verify(outboxMapper, never()).updateById(any());
    }

    // ======================== 辅助方法 ========================

    private ReceiptOutbox buildOutbox(Long id, Long receiptId, String messageBody,
                                       int status, int retryCount, LocalDateTime nextRetryTime) {
        ReceiptOutbox outbox = new ReceiptOutbox();
        outbox.setId(id);
        outbox.setReceiptId(receiptId);
        outbox.setMessageTopic("receipt-confirm");
        outbox.setMessageTag("confirm");
        outbox.setMessageBody(messageBody);
        outbox.setStatus(status);
        outbox.setRetryCount(retryCount);
        outbox.setNextRetryTime(nextRetryTime);
        outbox.setCreatedAt(LocalDateTime.now().minusHours(1));
        return outbox;
    }
}
