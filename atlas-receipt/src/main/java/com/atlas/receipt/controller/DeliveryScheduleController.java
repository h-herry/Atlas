package com.atlas.receipt.controller;

import com.atlas.common.core.web.Result;
import com.atlas.receipt.service.DeliveryScheduleService;
import com.atlas.receipt.service.DeliveryScheduleService.DailySchedule;
import com.atlas.receipt.service.DeliveryScheduleService.WeeklySchedule;
import com.atlas.common.security.annotation.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 发货排程看板控制器 / Delivery schedule dashboard controller
 * <p>
 * 提供当日到货计划与周维度到货汇总看板数据。 /
 * Provides daily arrival plan and weekly summary dashboard data.
 * </p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@RestController
@RequestMapping("/api/delivery/schedule")
@RequiredArgsConstructor
@Tag(name = "交货计划管理 / Delivery Schedule Management")
public class DeliveryScheduleController {

    private final DeliveryScheduleService deliveryScheduleService;

    /**
     * 当日到货计划 / Daily delivery schedule
     *
     * @param date 日期（不传则默认今天） / Date (defaults to today)
     */
    @GetMapping
    @RequirePermission("delivery:schedule:view")
    public Result<DailySchedule> daily(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        return Result.success(deliveryScheduleService.daily(date));
    }

    /**
     * 周维度到货汇总 / Weekly delivery summary
     *
     * @param referenceDate 参考日期（不传则默认本周） / Reference date (defaults to current week)
     */
    @GetMapping("/weekly")
    @RequirePermission("delivery:schedule:view")
    public Result<WeeklySchedule> weekly(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate) {
        return Result.success(deliveryScheduleService.weekly(referenceDate));
    }
}
