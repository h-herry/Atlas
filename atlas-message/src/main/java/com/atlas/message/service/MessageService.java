package com.atlas.message.service;

import com.atlas.message.dto.MessageRecord;
import com.atlas.message.dto.UnreadCountResponse;
import com.atlas.message.mapper.MessageMapper;
import com.atlas.message.model.Message;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息记录 Service — 消息持久化与查询 /
 * Message record Service — message persistence and query
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;

    /**
     * 保存消息记录 / Save message record
     */
    @Transactional(rollbackFor = Exception.class)
    public Message save(Message message) {
        message.setIsRead(message.getIsRead() != null ? message.getIsRead() : 0);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);
        log.debug("消息已持久化: id={}, type={}, supplierId={}", message.getId(), message.getType(), message.getSupplierId());
        return message;
    }

    /**
     * 分页查询供应商消息 / Paginated query supplier messages
     */
    public Page<MessageRecord> pageBySupplier(Long supplierId, String type, Integer isRead, int page, int size) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getSupplierId, supplierId);
        if (type != null && !type.isBlank()) {
            wrapper.eq(Message::getType, type);
        }
        if (isRead != null) {
            wrapper.eq(Message::getIsRead, isRead);
        }
        wrapper.orderByDesc(Message::getCreatedAt);

        Page<Message> msgPage = messageMapper.selectPage(new Page<>(page, size), wrapper);
        return toRecordPage(msgPage);
    }

    /**
     * 获取供应商未读消息列表 / Get supplier unread message list
     */
    public List<MessageRecord> getUnreadMessages(Long supplierId, int limit) {
        List<Message> messages = messageMapper.findUnreadBySupplier(supplierId, limit);
        return messages.stream().map(this::toRecord).collect(Collectors.toList());
    }

    /**
     * 获取未读计数 / Get unread count
     */
    public UnreadCountResponse getUnreadCount(Long supplierId) {
        Long total = messageMapper.countUnread(supplierId);
        List<Map<String, Object>> byType = messageMapper.countUnreadByType(supplierId);

        UnreadCountResponse response = UnreadCountResponse.builder()
                .supplierId(supplierId)
                .totalUnread(total)
                .build();

        for (Map<String, Object> row : byType) {
            String type = String.valueOf(row.get("type"));
            Long cnt = ((Number) row.get("cnt")).longValue();
            switch (type) {
                case "ORDER" -> response.setOrderUnread(cnt);
                case "DELIVERY" -> response.setDeliveryUnread(cnt);
                case "SETTLEMENT" -> response.setSettlementUnread(cnt);
                case "SYSTEM" -> response.setSystemUnread(cnt);
                case "APPROVAL" -> response.setApprovalUnread(cnt);
                case "QUALITY" -> response.setQualityUnread(cnt);
                default -> log.warn("未知消息事件类型: {}", type);
            }
        }
        return response;
    }

    /**
     * 标记消息为已读 / Mark messages as read
     */
    @Transactional(rollbackFor = Exception.class)
    public int markAsRead(Long supplierId, String type) {
        int rows = messageMapper.markAsRead(supplierId, type);
        log.info("消息已标记已读: supplierId={}, type={}, rows={}", supplierId, type, rows);
        return rows;
    }

    /**
     * 按ID查询消息 / Query message by ID
     */
    public Message getById(Long id) {
        return messageMapper.selectById(id);
    }

    /**
     * 标记单条消息已读 / Mark single message as read
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean readMessage(Long id) {
        Message msg = getById(id);
        if (msg == null) {
            return false;
        }
        msg.markAsRead();
        return messageMapper.updateById(msg) > 0;
    }

    /**
     * 批量标记消息为已读 / Batch mark messages as read
     *
     * @param messageIds 消息ID列表 / Message ID list
     * @return 已标记数量 / Count of marked messages
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchMarkAsRead(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Long id : messageIds) {
            if (readMessage(id)) {
                count++;
            }
        }
        log.info("批量标记已读完成: total={}, success={}", messageIds.size(), count);
        return count;
    }

    /**
     * 按用户ID获取未读计数 / Get unread count by user ID
     *
     * @param userId 用户ID / User ID
     * @return 未读计数响应 / Unread count response
     */
    public UnreadCountResponse getUnreadCountByUser(Long userId) {
        Long total = messageMapper.countUnreadByUser(userId);
        return UnreadCountResponse.builder()
                .totalUnread(total)
                .build();
    }

    // ---- private helpers ----

    private MessageRecord toRecord(Message msg) {
        return MessageRecord.builder()
                .id(msg.getId()).supplierId(msg.getSupplierId()).userId(msg.getUserId())
                .title(msg.getTitle()).content(msg.getContent()).type(msg.getType())
                .relatedId(msg.getRelatedId()).relatedType(msg.getRelatedType())
                .channel(msg.getChannel()).priority(msg.getPriority()).isRead(msg.getIsRead())
                .readAt(msg.getReadAt()).createdAt(msg.getCreatedAt())
                .build();
    }

    private Page<MessageRecord> toRecordPage(Page<Message> msgPage) {
        Page<MessageRecord> recordPage = new Page<>(msgPage.getCurrent(), msgPage.getSize(), msgPage.getTotal());
        recordPage.setRecords(msgPage.getRecords().stream().map(this::toRecord).collect(Collectors.toList()));
        return recordPage;
    }
}
