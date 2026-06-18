package com.atlas.message.controller;

import com.atlas.common.web.Result;
import com.atlas.message.dto.MessageRecord;
import com.atlas.message.dto.UnreadCountResponse;
import com.atlas.message.service.MessageService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息历史查询 Controller — HTTP 接口 / Message history query Controller — HTTP endpoints
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Tag(name = "消息记录 / Message Records", description = "消息历史查询 / 未读统计 / 已读标记")
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * 分页查询供应商消息 / Paginated query supplier messages
     */
    @Operation(summary = "分页查询消息 / Paginated message query")
    @GetMapping("/supplier/{supplierId}")
    public Result<Page<MessageRecord>> pageBySupplier(
            @PathVariable Long supplierId,
            @Parameter(description = "消息类型 / Message type: ORDER/DELIVERY/SETTLEMENT/SYSTEM/APPROVAL/QUALITY")
            @RequestParam(required = false) String type,
            @Parameter(description = "是否已读: 0 未读 / 1 已读 / Is read: 0 unread / 1 read")
            @RequestParam(required = false) Integer isRead,
            @Parameter(description = "页码 / Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小 / Page size") @RequestParam(defaultValue = "20") int size) {
        return Result.ok(messageService.pageBySupplier(supplierId, type, isRead, page, size));
    }

    /**
     * 获取供应商未读消息列表 / Get supplier unread messages
     */
    @Operation(summary = "获取未读消息 / Get unread messages")
    @GetMapping("/supplier/{supplierId}/unread")
    public Result<List<MessageRecord>> getUnreadMessages(
            @PathVariable Long supplierId,
            @Parameter(description = "最大返回条数 / Max records") @RequestParam(defaultValue = "50") int limit) {
        return Result.ok(messageService.getUnreadMessages(supplierId, limit));
    }

    /**
     * 获取未读计数 / Get unread count
     */
    @Operation(summary = "获取未读计数 / Get unread count")
    @GetMapping("/supplier/{supplierId}/unread-count")
    public Result<UnreadCountResponse> getUnreadCount(@PathVariable Long supplierId) {
        return Result.ok(messageService.getUnreadCount(supplierId));
    }

    /**
     * 标记消息为已读 / Mark messages as read
     */
    @Operation(summary = "标记消息已读 / Mark messages as read")
    @PutMapping("/supplier/{supplierId}/read")
    public Result<Integer> markAsRead(
            @PathVariable Long supplierId,
            @Parameter(description = "消息类型，不传则标记全部 / Message type, mark all if not provided")
            @RequestParam(required = false) String type) {
        int rows = messageService.markAsRead(supplierId, type);
        return Result.ok(rows);
    }

    /**
     * 标记单条消息已读 / Mark single message as read
     */
    @Operation(summary = "标记单条消息已读 / Mark single message as read")
    @PutMapping("/{messageId}/read")
    public Result<Boolean> readMessage(@PathVariable Long messageId) {
        return Result.ok(messageService.readMessage(messageId));
    }

    // ---- P1-3.9.2 已读/未读追踪 / Read/unread tracking ----

    /**
     * 批量标记已读 POST /api/message/read/batch /
     * Batch mark messages as read
     */
    @Operation(summary = "批量标记已读 / Batch mark as read")
    @PostMapping("/read/batch")
    public Result<Integer> batchMarkAsRead(@RequestBody List<Long> messageIds) {
        return Result.ok(messageService.batchMarkAsRead(messageIds));
    }

    /**
     * 未读消息数 GET /api/message/unread/count /
     * Unread count for a user
     */
    @Operation(summary = "未读数统计 / Unread count")
    @GetMapping("/unread/count")
    public Result<UnreadCountResponse> unreadCount(
            @Parameter(description = "用户ID / User ID") @RequestParam Long userId) {
        return Result.ok(messageService.getUnreadCountByUser(userId));
    }
}
