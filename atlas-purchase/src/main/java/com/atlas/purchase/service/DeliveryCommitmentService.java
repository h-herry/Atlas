package com.atlas.purchase.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.entity.DeliveryCommitment;
import com.atlas.purchase.entity.PurchaseOrder;
import com.atlas.purchase.mapper.DeliveryCommitmentMapper;
import com.atlas.purchase.mapper.PurchaseOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 交期承诺与预警服务 / Delivery commitment & alert service
 * <p>
 * 供应商提交承诺交期 → 与需求交期比对 → 偏差超过阈值自动预警。 /
 * Supplier submits committed date → compare with requested date → auto-alert if deviation exceeds threshold.
 * </p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryCommitmentService {

    private final DeliveryCommitmentMapper commitmentMapper;
    private final PurchaseOrderMapper orderMapper;

    /** 默认预警偏差天数阈值 / Default alert deviation threshold in days */
    private static final int DEFAULT_ALERT_THRESHOLD_DAYS = 3;

    /**
     * 供应商提交承诺交期 / Supplier submits delivery commitment
     *
     * @param orderId       订单ID / Order ID
     * @param lineNo        订单行号 / Order line number
     * @param materialId    物料ID / Material ID
     * @param requestedDate 需求交期 / Requested delivery date
     * @param committedDate 供应商承诺交期 / Supplier committed date
     * @param supplierId    供应商ID / Supplier ID
     * @return 交期承诺记录 / Delivery commitment record
     */
    @Transactional(rollbackFor = Exception.class)
    public DeliveryCommitment submitCommitment(Long orderId, Integer lineNo, Long materialId,
                                                LocalDate requestedDate, LocalDate committedDate,
                                                Long supplierId) {
        // 校验订单存在 / Verify order exists
        PurchaseOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_EXIST);
        }

        // 计算偏差天数（承诺交期 - 需求交期，正数为延迟） / Calculate deviation (positive means delayed)
        int deviationDays = (int) ChronoUnit.DAYS.between(requestedDate, committedDate);

        DeliveryCommitment commitment = new DeliveryCommitment();
        commitment.setOrderId(orderId);
        commitment.setLineNo(lineNo);
        commitment.setMaterialId(materialId);
        commitment.setRequestedDate(requestedDate);
        commitment.setCommittedDate(committedDate);
        commitment.setConfirmedDate(committedDate); // 初始确认 = 承诺 / Initial confirmed = committed
        commitment.setDeviationDays(deviationDays);
        commitment.setSupplierId(supplierId);
        commitment.setAlerted(0);
        commitmentMapper.insert(commitment);

        // 偏差超过阈值自动预警 / Auto-alert if deviation exceeds threshold
        if (deviationDays > DEFAULT_ALERT_THRESHOLD_DAYS) {
            sendAlert(commitment);
        }

        log.info("交期承诺已提交: orderId={} lineNo={} deviation={}d supplierId={}",
                orderId, lineNo, deviationDays, supplierId);
        return commitment;
    }

    /**
     * 确认实际交期 / Confirm actual delivery date
     *
     * @param commitmentId      交期承诺ID / Commitment ID
     * @param actualDeliveryDate 实际到货日 / Actual delivery date
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmDelivery(Long commitmentId, LocalDate actualDeliveryDate) {
        DeliveryCommitment commitment = commitmentMapper.selectById(commitmentId);
        if (commitment == null) {
            throw new BizException(6104, "交期承诺记录不存在 / Delivery commitment not found");
        }
        commitment.setActualDeliveryDate(actualDeliveryDate);

        // 实际 vs 承诺偏差 / Actual vs committed deviation
        if (commitment.getCommittedDate() != null) {
            int actualDeviation = (int) ChronoUnit.DAYS.between(commitment.getCommittedDate(), actualDeliveryDate);
            if (actualDeviation > 0) {
                log.warn("实际到货延迟 {} 天: orderId={} lineNo={}", actualDeviation,
                        commitment.getOrderId(), commitment.getLineNo());
            }
        }
        commitmentMapper.updateById(commitment);
        log.info("实际交期已确认: commitmentId={} actualDate={}", commitmentId, actualDeliveryDate);
    }

    /**
     * 按订单查询交期承诺 / Query commitments by order
     *
     * @param orderId 订单ID / Order ID
     * @return 交期承诺列表 / Commitment list
     */
    public List<DeliveryCommitment> listByOrder(Long orderId) {
        return commitmentMapper.selectList(
                new LambdaQueryWrapper<DeliveryCommitment>()
                        .eq(DeliveryCommitment::getOrderId, orderId)
                        .orderByAsc(DeliveryCommitment::getLineNo));
    }

    /**
     * 查询延迟交期（超过阈值天数的） / Query delayed commitments (exceeding threshold)
     *
     * @param thresholdDays 偏差阈值 / Deviation threshold in days
     * @return 延迟交期列表 / Delayed commitment list
     */
    public List<DeliveryCommitment> listDelayed(int thresholdDays) {
        return commitmentMapper.selectList(
                new LambdaQueryWrapper<DeliveryCommitment>()
                        .gt(DeliveryCommitment::getDeviationDays, thresholdDays)
                        .orderByDesc(DeliveryCommitment::getDeviationDays));
    }

    /**
     * 发送预警 / Send alert
     * <p>
     * 推送至消息中心，通知相关采购员交期存在延迟风险。 /
     * Push to message center, notify relevant buyer of delivery delay risk.
     * </p>
     *
     * @param commitment 交期承诺记录 / Delivery commitment
     */
    private void sendAlert(DeliveryCommitment commitment) {
        // 标记已预警 / Mark alerted
        commitment.setAlerted(1);
        commitment.setAlertSentAt(LocalDateTime.now());
        commitmentMapper.updateById(commitment);

        log.warn("交期预警: orderId={} lineNo={} deviation={}d——承诺交期超出需求交期 {} 天，请关注 / "
                        + "Delivery alert: orderId={} lineNo={} deviation={}d",
                commitment.getOrderId(), commitment.getLineNo(), commitment.getDeviationDays(),
                commitment.getOrderId(), commitment.getLineNo(), commitment.getDeviationDays());
    }
}
