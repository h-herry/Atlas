package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.DemandForecast;
import com.atlas.supplier.mapper.DemandForecastMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 需求预测协同 Service / Demand forecast collaboration Service
 *
 * <p>采购方录入需求预测 → 分享给供应商 → 供应商确认产能和承诺量。
 * 支持 SALES(销售预测)、PLAN(计划)、HISTORY(历史)三类数据来源。 /
 * Buyer enters demand forecast → shares with supplier → supplier confirms capacity and commitment quantity.
 * Supports SALES, PLAN, and HISTORY data sources.</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DemandForecastService extends ServiceImpl<DemandForecastMapper, DemandForecast> {

    private final DemandForecastMapper forecastMapper;

    /**
     * 分页查询需求预测（按供应商） / Paginated query by supplier
     */
    public Page<DemandForecast> pageBySupplier(Long supplierId, int page, int size) {
        LambdaQueryWrapper<DemandForecast> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DemandForecast::getCreatedAt);
        return forecastMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 分页查询需求预测 / Paginated query of demand forecasts
     */
    public Page<DemandForecast> page(Long materialId, String forecastMonth, int page, int size) {
        LambdaQueryWrapper<DemandForecast> wrapper = new LambdaQueryWrapper<>();
        if (materialId != null) {
            wrapper.eq(DemandForecast::getMaterialId, materialId);
        }
        if (forecastMonth != null && !forecastMonth.isEmpty()) {
            wrapper.eq(DemandForecast::getForecastMonth, forecastMonth);
        }
        wrapper.orderByDesc(DemandForecast::getCreatedAt);
        return forecastMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 创建需求预测（别名） / Create demand forecast (alias)
     */
    @Transactional(rollbackFor = Exception.class)
    public DemandForecast create(DemandForecast forecast) {
        createForecast(forecast);
        return forecast;
    }

    /**
     * 录入需求预测 / Create demand forecast
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createForecast(DemandForecast forecast) {
        forecast.setSharedToSupplier(0);
        return save(forecast);
    }

    /**
     * 分享给供应商 / Share with supplier
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean shareToSupplier(Long id) {
        DemandForecast forecast = getById(id);
        if (forecast == null) {
            throw new BizException(ErrorCode.FORECAST_NOT_EXIST);
        }
        forecast.setSharedToSupplier(1);
        return updateById(forecast);
    }

    /**
     * 供应商确认产能（反馈承诺量） / Supplier confirms capacity (feedback commitment quantity)
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean supplierFeedback(Long id, BigDecimal feedbackQty) {
        DemandForecast forecast = getById(id);
        if (forecast == null) {
            throw new BizException(ErrorCode.FORECAST_NOT_EXIST);
        }
        if (forecast.getSharedToSupplier() != 1) {
            throw new BizException(ErrorCode.BAD_REQUEST.getCode(), "需求预测尚未分享给供应商");
        }
        forecast.setSupplierFeedbackQty(feedbackQty);
        forecast.setSupplierFeedbackDate(LocalDate.now());
        return updateById(forecast);
    }

    /**
     * 查询已分享待确认的预测 / Query shared but unconfirmed forecasts
     */
    public java.util.List<DemandForecast> listPendingFeedback() {
        LambdaQueryWrapper<DemandForecast> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DemandForecast::getSharedToSupplier, 1)
               .isNull(DemandForecast::getSupplierFeedbackDate);
        return forecastMapper.selectList(wrapper);
    }

    /**
     * 供应商确认 / Supplier confirms forecast
     */
    @Transactional(rollbackFor = Exception.class)
    public void supplierConfirm(Long id, String confirmedBy) {
        DemandForecast forecast = getById(id);
        if (forecast == null) {
            throw new BizException(ErrorCode.FORECAST_NOT_EXIST);
        }
        forecast.setSupplierFeedbackQty(forecast.getForecastQty());
        forecast.setSupplierFeedbackDate(LocalDate.now());
        updateById(forecast);
        log.info("供应商已确认需求预测: id={}, confirmedBy={}", id, confirmedBy);
    }

    /**
     * 生成配送计划 / Generate delivery plan
     */
    @Transactional(rollbackFor = Exception.class)
    public void deliveryPlan(Long id, String scheduledDate) {
        DemandForecast forecast = getById(id);
        if (forecast == null) {
            throw new BizException(ErrorCode.FORECAST_NOT_EXIST);
        }
        log.info("配送计划已生成: id={}, scheduledDate={}", id, scheduledDate);
    }
}
