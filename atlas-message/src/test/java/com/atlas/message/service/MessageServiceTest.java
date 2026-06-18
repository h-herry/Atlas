package com.atlas.message.service;

import com.atlas.message.dto.MessageRecord;
import com.atlas.message.dto.UnreadCountResponse;
import com.atlas.message.mapper.MessageMapper;
import com.atlas.message.model.Message;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 消息服务单元测试 / Message service unit tests
 *
 * @author Atlas Team
 * @since 1.2.24
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("消息服务单元测试")
class MessageServiceTest {

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageService messageService;

    // ======================== 消息持久化 ========================

    @Test
    @Order(1)
    @DisplayName("保存消息：isRead 默认为 0，createdAt 自动填充")
    void testSaveMessage() {
        Message message = new Message();
        message.setId(1L);
        message.setType("ORDER");
        message.setSupplierId(100L);
        message.setTitle("新采购订单通知");
        message.setContent("您有一笔新的采购订单待处理");

        when(messageMapper.insert(any(Message.class))).thenReturn(1);

        Message result = messageService.save(message);

        assertThat(result).isNotNull();
        assertThat(result.getIsRead()).isEqualTo(0);
        assertThat(result.getCreatedAt()).isNotNull();
        verify(messageMapper).insert(message);
    }

    // ======================== 标记已读 ========================

    @Test
    @Order(2)
    @DisplayName("标记单条消息已读：存在消息则更新成功")
    void testReadMessage() {
        Message message = new Message();
        message.setId(1L);
        message.setIsRead(0);

        when(messageMapper.selectById(1L)).thenReturn(message);
        when(messageMapper.updateById(any(Message.class))).thenReturn(1);

        boolean result = messageService.readMessage(1L);

        assertThat(result).isTrue();
        assertThat(message.getIsRead()).isEqualTo(1);
        assertThat(message.getReadAt()).isNotNull();
        verify(messageMapper).selectById(1L);
        verify(messageMapper).updateById(message);
    }

    @Test
    @Order(3)
    @DisplayName("标记不存在的消息已读：返回 false")
    void testReadMessage_NotFound() {
        when(messageMapper.selectById(999L)).thenReturn(null);

        boolean result = messageService.readMessage(999L);

        assertThat(result).isFalse();
        verify(messageMapper).selectById(999L);
        verify(messageMapper, never()).updateById(any());
    }
}