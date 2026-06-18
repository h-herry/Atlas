package com.atlas.purchase.service;

import cn.hutool.core.util.IdUtil;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.entity.OrderChange;
import com.atlas.purchase.entity.OrderChangeDetail;
import com.atlas.purchase.entity.PurchaseOrder;
import com.atlas.purchase.mapper.OrderChangeDetailMapper;
import com.atlas.purchase.mapper.OrderChangeMapper;
import com.atlas.purchase.mapper.PurchaseOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单变更管理（ECN）核心业务服务 / Order change management (ECN) core business service
 * <p>
 * 提供变更申请创建、审批流转、供应商确认及版本归档。 /
 * Provides change request creation, approval workflow, supplier confirmation, and version archiving.
 * </p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderChangeService {

    private final OrderChangeMapper changeMapper;
    private final OrderChangeDetailMapper detailMapper;
    private final PurchaseOrderMapper orderMapper;

    /** 变更状态常量 / Change status constants */
    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PENDING_APPROVE = "PENDING_APPROVE";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_SUPPLIER_CONFIRMED = "SUPPLIER_CONFIRMED";
    public static final String STATUS_EXECUTED = "EXECUTED";
    public static final String STATUS_REJECTED = "REJECTED";

    /**
     * 创建变更申请 / Create change request
     *
     * @param orderId      采购订单ID / Purchase order ID
     * @param changeType   变更类型 / Change type
     * @param changeReason 变更原因 / Change reason
     * @param details      变更明细列表 / Change detail list
     * @param createdBy    创建人 / Created by
     * @return 变更单 / Order change
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderChange createChange(Long orderId, String changeType, String changeReason,
                                     List<ChangeDetailRequest> details, Long createdBy) {
        // 校验订单存在 / Verify order exists
        PurchaseOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_EXIST);
        }

        // 生成变更单号 / Generate change number
        String changeNo = "ECN" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();

        // 插入变更主表 / Insert change master
        OrderChange change = new OrderChange();
        change.setChangeNo(changeNo);
        change.setOrderId(orderId);
        change.setChangeType(changeType);
        change.setChangeReason(changeReason);
        change.setStatus(STATUS_DRAFT);
        change.setCreatedBy(createdBy);
        changeMapper.insert(change);

        // 插入变更明细 / Insert change details
        if (details != null && !details.isEmpty()) {
            for (ChangeDetailRequest detailReq : details) {
                OrderChangeDetail detail = new OrderChangeDetail();
                detail.setChangeId(change.getId());
                detail.setFieldName(detailReq.getFieldName());
                detail.setFieldLabel(detailReq.getFieldLabel());
                detail.setOldValue(detailReq.getOldValue());
                detail.setNewValue(detailReq.getNewValue());
                detailMapper.insert(detail);
            }
        }

        log.info("变更申请创建成功: changeNo={} orderId={} type={}", changeNo, orderId, changeType);
        return change;
    }

    /**
     * 提交变更审批 / Submit change for approval
     *
     * @param changeId 变更单ID / Change ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitApproval(Long changeId) {
        OrderChange change = changeMapper.selectById(changeId);
        if (change == null) {
            throw new BizException(ErrorCode.CHANGE_NOT_EXIST);
        }
        if (!STATUS_DRAFT.equals(change.getStatus())) {
            throw new BizException(6001, "仅草稿状态的变更单可提交审批 / Only draft changes can be submitted");
        }
        change.setStatus(STATUS_PENDING_APPROVE);
        changeMapper.updateById(change);
        log.info("变更单已提交审批: changeNo={}", change.getChangeNo());
    }

    /**
     * 审批变更（通过/驳回） / Approve or reject change
     *
     * @param changeId   变更单ID / Change ID
     * @param approved   是否通过 / Whether approved
     * @param approvedBy 审批人ID / Approver ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long changeId, boolean approved, Long approvedBy) {
        OrderChange change = changeMapper.selectById(changeId);
        if (change == null) {
            throw new BizException(ErrorCode.CHANGE_NOT_EXIST);
        }
        if (!STATUS_PENDING_APPROVE.equals(change.getStatus())) {
            throw new BizException(6002, "仅待审批状态的变更单可审批 / Only pending-approval changes can be approved");
        }

        change.setApprovedBy(approvedBy);
        change.setApprovedAt(LocalDateTime.now());

        if (approved) {
            change.setStatus(STATUS_APPROVED);
            log.info("变更单已审批通过: changeNo={} approvedBy={}", change.getChangeNo(), approvedBy);
        } else {
            change.setStatus(STATUS_REJECTED);
            log.info("变更单已驳回: changeNo={} approvedBy={}", change.getChangeNo(), approvedBy);
        }
        changeMapper.updateById(change);
    }

    /**
     * 供应商确认变更 / Supplier confirms change
     *
     * @param changeId 变更单ID / Change ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void supplierConfirm(Long changeId) {
        OrderChange change = changeMapper.selectById(changeId);
        if (change == null) {
            throw new BizException(ErrorCode.CHANGE_NOT_EXIST);
        }
        if (!STATUS_APPROVED.equals(change.getStatus())) {
            throw new BizException(6003, "仅已审批状态的变更单可供应商确认 / Only approved changes can be confirmed by supplier");
        }
        change.setStatus(STATUS_SUPPLIER_CONFIRMED);
        change.setSupplierConfirmedAt(LocalDateTime.now());
        changeMapper.updateById(change);
        log.info("供应商已确认变更: changeNo={}", change.getChangeNo());
    }

    /**
     * 执行变更 / Execute change
     *
     * @param changeId 变更单ID / Change ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void execute(Long changeId) {
        OrderChange change = changeMapper.selectById(changeId);
        if (change == null) {
            throw new BizException(ErrorCode.CHANGE_NOT_EXIST);
        }
        if (!STATUS_SUPPLIER_CONFIRMED.equals(change.getStatus())) {
            throw new BizException(6004, "仅供应商已确认的变更单可执行 / Only supplier-confirmed changes can be executed");
        }
        change.setStatus(STATUS_EXECUTED);
        changeMapper.updateById(change);
        log.info("变更已执行: changeNo={}", change.getChangeNo());
    }

    /**
     * 查询变更单详情 / Query change detail with items
     *
     * @param changeId 变更单ID / Change ID
     * @return 变更单 / Order change
     */
    public OrderChange getById(Long changeId) {
        OrderChange change = changeMapper.selectById(changeId);
        if (change == null) {
            throw new BizException(ErrorCode.CHANGE_NOT_EXIST);
        }
        return change;
    }

    /**
     * 查询变更明细列表 / Query change detail list
     *
     * @param changeId 变更单ID / Change ID
     * @return 变更明细列表 / Change detail list
     */
    public List<OrderChangeDetail> listDetails(Long changeId) {
        return detailMapper.selectList(
                new LambdaQueryWrapper<OrderChangeDetail>().eq(OrderChangeDetail::getChangeId, changeId));
    }

    /**
     * 分页查询变更单 / Paginated query of changes
     *
     * @param orderId 采购订单ID（可选） / Purchase order ID (optional)
     * @param status  状态（可选） / Status (optional)
     * @param page    当前页 / Current page
     * @param size    每页大小 / Page size
     * @return 分页结果 / Paginated result
     */
    public Page<OrderChange> page(Long orderId, String status, int page, int size) {
        LambdaQueryWrapper<OrderChange> wrapper = new LambdaQueryWrapper<>();
        if (orderId != null) {
            wrapper.eq(OrderChange::getOrderId, orderId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(OrderChange::getStatus, status);
        }
        wrapper.orderByDesc(OrderChange::getCreatedAt);
        return changeMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ==================== 内部类 / Inner Classes ====================

    /**
     * 变更明细创建请求 / Change detail creation request
     */
    @lombok.Data
    public static class ChangeDetailRequest {
        /** 变更字段名 / Changed field name */
        private String fieldName;
        /** 字段显示名 / Field display label */
        private String fieldLabel;
        /** 变更前值 / Old value */
        private String oldValue;
        /** 变更后值 / New value */
        private String newValue;
    }
}
