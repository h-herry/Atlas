package com.atlas.order.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.order.entity.JitDeliverySchedule;
import com.atlas.order.service.JitDeliveryService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JIT 交货排程 REST API / JIT Delivery Schedule REST API
 * <p>
 * 提供供应商确认交货窗口、查询排程等接口。 /
 * Provides supplier confirmation of delivery window, schedule query endpoints.
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@RestController
@RequestMapping("/api/order/jit")
@RequiredArgsConstructor
public class JitScheduleController {

    private final JitDeliveryService jitDeliveryService;

    /**
     * 供应商确认 JIT 交货排程 / Supplier confirms JIT delivery schedule
     * <p>
     * 必须在交货窗口内（默认 08:00-14:00）调用，超时无法确认。 /
     * Must be called within the delivery window (default 08:00-14:00); cannot confirm after window expires.
     *
     * @param orderId 订单ID / Order ID
     * @return 更新后的排程 / Updated schedule
     */
    @PostMapping("/confirm")
    @RequirePermission("order:jit:confirm")
    public Result<JitDeliverySchedule> confirm(@RequestParam @NotNull Long orderId) {
        JitDeliverySchedule schedule = jitDeliveryService.confirmSchedule(orderId);
        return Result.ok(schedule);
    }

    /**
     * 供应商排程查询 / Supplier schedule query
     * <p>
     * 按交货日期范围查询排程，支持状态过滤。 /
     * Query schedules by delivery date range, with optional status filter.
     *
     * @param startDate 开始日期 / Start date
     * @param endDate   结束日期 / End date
     * @param status    状态过滤: PENDING/CONFIRMED/MISSED / Status filter
     * @return 排程列表 / Schedule list
     */
    @GetMapping("/schedule")
    @RequirePermission("order:jit:view")
    public Result<List<JitDeliverySchedule>> listSchedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status) {
        return Result.ok(jitDeliveryService.listSchedules(startDate, endDate, status));
    }

    /**
     * 查询单个订单的 JIT 排程及剩余时间 / Query single order JIT schedule with remaining time
     *
     * @param orderId 订单ID / Order ID
     * @return 排程及窗口剩余信息 / Schedule with window remaining info
     */
    @GetMapping("/schedule/{orderId}")
    @RequirePermission("order:jit:view")
    public Result<Map<String, Object>> getByOrderId(@PathVariable Long orderId) {
        JitDeliverySchedule schedule = jitDeliveryService.getByOrderId(orderId);
        long remainingMinutes = jitDeliveryService.getRemainingMinutes(schedule);

        Map<String, Object> result = new HashMap<>();
        result.put("schedule", schedule);
        result.put("remainingMinutes", remainingMinutes);
        result.put("windowExpired", remainingMinutes < 0);
        return Result.ok(result);
    }
}
