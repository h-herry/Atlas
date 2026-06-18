package com.atlas.receipt.service;

import com.atlas.receipt.mapper.AsnRecordMapper;
import com.atlas.receipt.mapper.ReceivingRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 发货排程看板服务 / Delivery schedule dashboard service
 * <p>
 * 提供当日到货计划、周维度到货汇总看板数据。 /
 * Provides daily arrival plan and weekly arrival summary dashboard data.
 * 看板维度：预计到货 / 实际到货 / 延迟 / 紧急插单标记。 /
 * Dashboard dimensions: expected arrival / actual arrival / delayed / urgent insertion flag.
 * </p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryScheduleService {

    private final AsnRecordMapper asnRecordMapper;
    private final ReceivingRecordMapper receivingRecordMapper;

    /**
     * 当日到货计划 / Daily delivery schedule
     *
     * @param date 日期 / Date
     * @return 当日到货计划 / Daily schedule
     */
    public DailySchedule daily(LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

        DailySchedule schedule = new DailySchedule();
        schedule.setDate(date);

        // 预计到货 (从 ASN 查询) / Expected arrivals (from ASN)
        List<ScheduleEntry> expectedList = new ArrayList<>();
        var asnRecords = asnRecordMapper.selectList(
                new LambdaQueryWrapper<com.atlas.receipt.entity.AsnRecord>()
                        .eq(com.atlas.receipt.entity.AsnRecord::getExpectedArrivalDate, date));
        for (var asn : asnRecords) {
            ScheduleEntry entry = new ScheduleEntry();
            entry.setAsnNo(asn.getAsnNo());
            entry.setOrderId(asn.getOrderId());
            entry.setSupplierId(asn.getSupplierId());
            entry.setCarrier(asn.getCarrier());
            entry.setTrackingNo(asn.getTrackingNo());
            entry.setExpectedArrivalDate(asn.getExpectedArrivalDate());
            entry.setEntryType("EXPECTED");
            expectedList.add(entry);
        }
        schedule.setExpected(expectedList);
        schedule.setExpectedCount(expectedList.size());

        // 实际到货 (从收货记录查询) / Actual arrivals (from receiving records)
        List<ScheduleEntry> actualList = new ArrayList<>();
        var receivingRecords = receivingRecordMapper.selectList(
                new LambdaQueryWrapper<com.atlas.receipt.entity.ReceivingRecord>()
                        .between(com.atlas.receipt.entity.ReceivingRecord::getReceiveDate, dayStart, dayEnd));
        for (var recv : receivingRecords) {
            ScheduleEntry entry = new ScheduleEntry();
            entry.setReceiveNo(recv.getReceiveNo());
            entry.setOrderId(recv.getOrderId());
            entry.setAsnId(recv.getAsnId());
            entry.setActualArrivalTime(recv.getReceiveDate());
            entry.setEntryType("ACTUAL");
            actualList.add(entry);
        }
        schedule.setActual(actualList);
        schedule.setActualCount(actualList.size());

        // 延迟：预计到货日在前但当天未实际到货 / Delayed: expected today but not yet received
        schedule.setDelayed(Math.max(0, schedule.getExpectedCount() - schedule.getActualCount()));

        log.info("发货排程日看板: date={} expected={} actual={} delayed={}",
                date, schedule.getExpectedCount(), schedule.getActualCount(), schedule.getDelayed());
        return schedule;
    }

    /**
     * 周维度到货汇总 / Weekly delivery summary
     *
     * @param referenceDate 参考日期（默认本周） / Reference date (defaults to current week)
     * @return 周到货汇总 / Weekly summary
     */
    public WeeklySchedule weekly(LocalDate referenceDate) {
        if (referenceDate == null) {
            referenceDate = LocalDate.now();
        }

        // 计算本周周一和周日 / Calculate week's Monday and Sunday
        LocalDate weekStart = referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        WeeklySchedule weekly = new WeeklySchedule();
        weekly.setWeekStart(weekStart);
        weekly.setWeekEnd(weekEnd);

        List<DailySummary> dailySummaries = new ArrayList<>();
        int totalExpected = 0;
        int totalActual = 0;
        int totalDelayed = 0;

        for (LocalDate d = weekStart; !d.isAfter(weekEnd); d = d.plusDays(1)) {
            DailySchedule daily = daily(d);
            DailySummary summary = new DailySummary();
            summary.setDate(d);
            summary.setDayOfWeek(d.getDayOfWeek().getValue());
            summary.setExpectedCount(daily.getExpectedCount());
            summary.setActualCount(daily.getActualCount());
            summary.setDelayed(daily.getDelayed());

            // 紧急插单标记：如果当日预计到货中有紧急 ASN 则标记 /
            // Urgent flag: mark if any urgent ASN exists in today's expected arrivals
            summary.setHasUrgent(false); // 实际应依据 ASN 标记判断 / In production, check ASN urgent flag

            dailySummaries.add(summary);
            totalExpected += daily.getExpectedCount();
            totalActual += daily.getActualCount();
            totalDelayed += daily.getDelayed();
        }

        weekly.setDailyDetails(dailySummaries);
        weekly.setTotalExpected(totalExpected);
        weekly.setTotalActual(totalActual);
        weekly.setTotalDelayed(totalDelayed);

        log.info("发货排程周看板: week={}~{} expected={} actual={} delayed={}",
                weekStart, weekEnd, totalExpected, totalActual, totalDelayed);
        return weekly;
    }

    // ==================== 内部数据类 / Inner Data Classes ====================

    /**
     * 当日到货计划 / Daily schedule
     */
    @lombok.Data
    public static class DailySchedule {
        private LocalDate date;
        private List<ScheduleEntry> expected;
        private int expectedCount;
        private List<ScheduleEntry> actual;
        private int actualCount;
        private int delayed;
    }

    /**
     * 排程条目 / Schedule entry
     */
    @lombok.Data
    public static class ScheduleEntry {
        private String asnNo;
        private String receiveNo;
        private Long orderId;
        private Long supplierId;
        private Long asnId;
        private String carrier;
        private String trackingNo;
        private LocalDate expectedArrivalDate;
        private LocalDateTime actualArrivalTime;
        private String entryType; // EXPECTED / ACTUAL
        private boolean urgent;
    }

    /**
     * 周到货汇总 / Weekly schedule
     */
    @lombok.Data
    public static class WeeklySchedule {
        private LocalDate weekStart;
        private LocalDate weekEnd;
        private List<DailySummary> dailyDetails;
        private int totalExpected;
        private int totalActual;
        private int totalDelayed;
    }

    /**
     * 日维度汇总 / Daily summary
     */
    @lombok.Data
    public static class DailySummary {
        private LocalDate date;
        private int dayOfWeek;
        private int expectedCount;
        private int actualCount;
        private int delayed;
        private boolean hasUrgent;
    }
}
