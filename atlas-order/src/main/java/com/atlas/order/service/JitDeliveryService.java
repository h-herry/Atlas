package com.atlas.order.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.order.entity.JitDeliverySchedule;
import com.atlas.order.mapper.JitDeliveryScheduleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * JIT交货服务 — 交货窗口计算、超时检测、确认管理 /
 * JIT delivery service — delivery window calculation, timeout detection, confirmation management
 * <p>
 * 汽车行业 JIT 模式核心逻辑： /
 * Automotive JIT mode core logic:
 * <ol>
 *   <li>订单发布后按交货日期生成排程，计算确认窗口（默认当天 08:00-14:00） / After order release, generate schedule by delivery date, calc window (default 08:00-14:00)</li>
 *   <li>供应商必须在窗口内确认 / Supplier must confirm within window</li>
 *   <li>定时任务扫描超时未确认的排程，标记 MISSED 并触发预警 / Scheduled task scans for overdue schedules, marks MISSED and triggers alert</li>
 * </ol>
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JitDeliveryService {

    private final JitDeliveryScheduleMapper scheduleMapper;

    /** 默认交货窗口开始时间: 08:00 / Default delivery window start: 08:00 */
    public static final LocalTime DEFAULT_WINDOW_START = LocalTime.of(8, 0);

    /** 默认交货窗口结束时间: 14:00 / Default delivery window end: 14:00 */
    public static final LocalTime DEFAULT_WINDOW_END = LocalTime.of(14, 0);

    /**
     * 订单发布后生成 JIT 交货排程 / Generate JIT delivery schedule after order release
     * <p>
     * 根据交货日期计算确认窗口，写入排程记录。 /
     * Calculate confirmation window based on delivery date, write schedule record.
     *
     * @param orderId      订单ID / Order ID
     * @param deliveryDate 交货日期 / Delivery date
     * @return 排程记录 / Schedule record
     */
    @Transactional(rollbackFor = Exception.class)
    public JitDeliverySchedule createSchedule(Long orderId, LocalDate deliveryDate) {
        return createSchedule(orderId, deliveryDate, DEFAULT_WINDOW_START, DEFAULT_WINDOW_END);
    }

    /**
     * 订单发布后生成 JIT 交货排程（自定义窗口） / Generate JIT schedule with custom window
     *
     * @param orderId      订单ID / Order ID
     * @param deliveryDate 交货日期 / Delivery date
     * @param windowStart  窗口开始时间 / Window start
     * @param windowEnd    窗口结束时间 / Window end
     * @return 排程记录 / Schedule record
     */
    @Transactional(rollbackFor = Exception.class)
    public JitDeliverySchedule createSchedule(Long orderId, LocalDate deliveryDate,
                                               LocalTime windowStart, LocalTime windowEnd) {
        // 校验窗口合法性 / Validate window validity
        if (!windowStart.isBefore(windowEnd)) {
            throw new BizException(ErrorCode.PARAM_INVALID.getCode(), "交货窗口开始时间必须早于结束时间");
        }

        JitDeliverySchedule schedule = new JitDeliverySchedule();
        schedule.setOrderId(orderId);
        schedule.setDeliveryDate(deliveryDate);
        schedule.setWindowStart(windowStart);
        schedule.setWindowEnd(windowEnd);
        schedule.setStatus(JitDeliverySchedule.STATUS_PENDING);
        scheduleMapper.insert(schedule);

        log.info("JIT排程已创建: orderId={} deliveryDate={} window={}-{}",
                orderId, deliveryDate, windowStart, windowEnd);
        return schedule;
    }

    /**
     * 供应商确认 JIT 交货排程 / Supplier confirms JIT delivery schedule
     *
     * @param orderId 订单ID / Order ID
     * @return 更新后的排程 / Updated schedule
     */
    @Transactional(rollbackFor = Exception.class)
    public JitDeliverySchedule confirmSchedule(Long orderId) {
        JitDeliverySchedule schedule = scheduleMapper.selectOne(
                new LambdaQueryWrapper<JitDeliverySchedule>()
                        .eq(JitDeliverySchedule::getOrderId, orderId)
                        .orderByDesc(JitDeliverySchedule::getCreatedAt)
                        .last("LIMIT 1"));

        if (schedule == null) {
            throw new BizException(ErrorCode.JIT_SCHEDULE_NOT_EXIST.getCode(), "JIT排程不存在");
        }

        // 校验窗口：必须在窗口时间内确认 / Validate window: must confirm within the window
        LocalTime nowTime = LocalTime.now();
        if (nowTime.isBefore(schedule.getWindowStart()) || nowTime.isAfter(schedule.getWindowEnd())) {
            throw new BizException(ErrorCode.JIT_OUTSIDE_WINDOW.getCode(), String.format(
                    "当前不在确认窗口内，窗口时间为 %s ~ %s",
                    schedule.getWindowStart(), schedule.getWindowEnd()));
        }

        if (!JitDeliverySchedule.STATUS_PENDING.equals(schedule.getStatus())) {
            throw new BizException(ErrorCode.JIT_ALREADY_CONFIRMED.getCode(),
                    "排程状态为" + schedule.getStatus() + "，不可重复确认");
        }

        schedule.setStatus(JitDeliverySchedule.STATUS_CONFIRMED);
        schedule.setConfirmTime(LocalDateTime.now());
        scheduleMapper.updateById(schedule);

        log.info("JIT排程已确认: orderId={} scheduleId={}", orderId, schedule.getId());
        return schedule;
    }

    /**
     * 定时任务：扫描超时未确认的排程，标记 MISSED / Scheduled task: scan overdue schedules, mark MISSED
     * <p>
     * 每 10 分钟执行一次，扫描当天交货日期的待确认排程，当前时间超过窗口结束时间则标记 MISSED。 /
     * Runs every 10 minutes; scans PENDING schedules with today's delivery date;
     * marks as MISSED if current time exceeds window end.
     */
    @Scheduled(fixedDelay = 600000)
    @Transactional(rollbackFor = Exception.class)
    public void scanMissedSchedules() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<JitDeliverySchedule> pendings = scheduleMapper.selectList(
                new LambdaQueryWrapper<JitDeliverySchedule>()
                        .eq(JitDeliverySchedule::getDeliveryDate, today)
                        .eq(JitDeliverySchedule::getStatus, JitDeliverySchedule.STATUS_PENDING));

        int missedCount = 0;
        for (JitDeliverySchedule s : pendings) {
            if (now.isAfter(s.getWindowEnd())) {
                s.setStatus(JitDeliverySchedule.STATUS_MISSED);
                scheduleMapper.updateById(s);
                missedCount++;
                log.warn("JIT排程超时未确认: orderId={} scheduleId={} deliveryDate={} window={}-{}",
                        s.getOrderId(), s.getId(), s.getDeliveryDate(),
                        s.getWindowStart(), s.getWindowEnd());
            }
        }

        if (missedCount > 0) {
            log.info("JIT超时扫描完成: 扫描{}条，标记MISSED {}条", pendings.size(), missedCount);
        }
    }

    /**
     * 查询供应商排程（按交货日期范围） / Query supplier schedules by delivery date range
     *
     * @param startDate 开始日期 / Start date
     * @param endDate   结束日期 / End date
     * @param status    状态过滤（可选） / Status filter (optional)
     * @return 排程列表 / Schedule list
     */
    public List<JitDeliverySchedule> listSchedules(LocalDate startDate, LocalDate endDate, String status) {
        LambdaQueryWrapper<JitDeliverySchedule> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null) {
            wrapper.ge(JitDeliverySchedule::getDeliveryDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(JitDeliverySchedule::getDeliveryDate, endDate);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(JitDeliverySchedule::getStatus, status);
        }
        wrapper.orderByAsc(JitDeliverySchedule::getDeliveryDate)
               .orderByAsc(JitDeliverySchedule::getWindowStart);
        return scheduleMapper.selectList(wrapper);
    }

    /**
     * 按订单ID查询排程 / Query schedule by order ID
     *
     * @param orderId 订单ID / Order ID
     * @return 排程记录 / Schedule record
     */
    public JitDeliverySchedule getByOrderId(Long orderId) {
        return scheduleMapper.selectOne(
                new LambdaQueryWrapper<JitDeliverySchedule>()
                        .eq(JitDeliverySchedule::getOrderId, orderId)
                        .orderByDesc(JitDeliverySchedule::getCreatedAt)
                        .last("LIMIT 1"));
    }

    /**
     * 获取窗口剩余时间（分钟） / Get remaining window time (minutes)
     *
     * @param schedule 排程记录 / Schedule record
     * @return 剩余分钟数（负数表示已过期） / Remaining minutes (negative means expired)
     */
    public long getRemainingMinutes(JitDeliverySchedule schedule) {
        LocalTime now = LocalTime.now();
        return Duration.between(now, schedule.getWindowEnd()).toMinutes();
    }
}
