package com.atlas.supplier.service;

import cn.hutool.core.util.StrUtil;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.MrpPlan;
import com.atlas.supplier.entity.MrpResult;
import com.atlas.supplier.mapper.MrpPlanMapper;
import com.atlas.supplier.mapper.MrpResultMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MRP 需求计划 Service / MRP demand planning Service
 *
 * <p>创建计划 → 计算净需求（毛需求 - 库存 - 在途）→ 生成建议采购量。 /
 * Create plan → calculate net demand (gross - stock - in-transit) → generate suggested purchase quantity.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MrpService {

    private final MrpPlanMapper planMapper;
    private final MrpResultMapper resultMapper;

    /**
     * 创建 MRP 计划 / Create MRP plan
     */
    @Transactional(rollbackFor = Exception.class)
    public MrpPlan create(MrpPlan plan) {
        plan.setStatus(0);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        planMapper.insert(plan);
        log.info("MRP计划创建成功: planNo={}", plan.getPlanNo());
        return plan;
    }

    /**
     * 执行 MRP 计算：生成建议采购量和时间 / Execute MRP calculation: generate suggested purchase quantity and timeline
     */
    @Transactional(rollbackFor = Exception.class)
    public void calculate(Long planId) {
        MrpPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new BizException(ErrorCode.MRP_PLAN_NOT_EXIST);
        }
        if (plan.getStatus() >= 2) {
            throw new BizException(ErrorCode.MRP_ALREADY_CONFIRMED);
        }
        // 清空旧计算结果 / Clear previous calculation results
        resultMapper.delete(
                new LambdaQueryWrapper<MrpResult>().eq(MrpResult::getPlanId, planId));

        // 生成示例计算结果（真实场景需接入库存/在途数据源） /
        // Generate example calculation results (real scenario requires stock/in-transit data sources)
        MrpResult example = new MrpResult();
        example.setPlanId(planId);
        example.setMaterialId(1L);
        example.setGrossDemand(new BigDecimal("1000.00"));
        example.setCurrentStock(new BigDecimal("200.00"));
        example.setInTransit(new BigDecimal("50.00"));
        BigDecimal netDemand = example.getGrossDemand()
                .subtract(example.getCurrentStock())
                .subtract(example.getInTransit());
        example.setNetDemand(netDemand.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : netDemand);
        example.setPlannedOrderQty(example.getNetDemand().multiply(new BigDecimal("1.1"))
                .setScale(2, RoundingMode.HALF_UP));
        example.setPlannedStartDate(java.time.LocalDate.now().plusDays(7));
        example.setPlannedEndDate(java.time.LocalDate.now().plusDays(21));
        example.setCreatedAt(LocalDateTime.now());
        resultMapper.insert(example);

        plan.setStatus(1);
        plan.setUpdatedAt(LocalDateTime.now());
        planMapper.updateById(plan);
        log.info("MRP计算完成: planId={} netDemand={}", planId, example.getNetDemand());
    }

    /**
     * 确认 MRP 计划 / Confirm MRP plan
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long planId) {
        MrpPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new BizException(ErrorCode.MRP_PLAN_NOT_EXIST);
        }
        if (plan.getStatus() != 1) {
            throw new BizException(12009, "仅'已计算'状态的MRP计划可确认");
        }
        plan.setStatus(2);
        plan.setUpdatedAt(LocalDateTime.now());
        planMapper.updateById(plan);
        log.info("MRP计划确认: planId={}", planId);
    }

    /**
     * 下发 MRP 计划 / Issue MRP plan
     */
    @Transactional(rollbackFor = Exception.class)
    public void issue(Long planId) {
        MrpPlan plan = planMapper.selectById(planId);
        if (plan == null) {
            throw new BizException(ErrorCode.MRP_PLAN_NOT_EXIST);
        }
        if (plan.getStatus() != 2) {
            throw new BizException(12009, "仅'已确认'状态的MRP计划可下发");
        }
        plan.setStatus(3);
        plan.setUpdatedAt(LocalDateTime.now());
        planMapper.updateById(plan);
        log.info("MRP计划下发: planId={}", planId);
    }

    /**
     * 查询 MRP 计划 / Query MRP plan
     */
    public MrpPlan getById(Long id) {
        MrpPlan plan = planMapper.selectById(id);
        if (plan == null) {
            throw new BizException(ErrorCode.MRP_PLAN_NOT_EXIST);
        }
        return plan;
    }

    /**
     * 分页查询 MRP 计划 / Paginated query of MRP plans
     */
    public Page<MrpPlan> page(String keyword, Integer status, int page, int size) {
        LambdaQueryWrapper<MrpPlan> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(MrpPlan::getPlanNo, keyword);
        }
        if (status != null) {
            wrapper.eq(MrpPlan::getStatus, status);
        }
        wrapper.orderByDesc(MrpPlan::getCreatedAt);
        return planMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 查询 MRP 计算结果 / Query MRP calculation results
     */
    public List<MrpResult> listResults(Long planId) {
        return resultMapper.selectList(
                new LambdaQueryWrapper<MrpResult>().eq(MrpResult::getPlanId, planId));
    }
}
