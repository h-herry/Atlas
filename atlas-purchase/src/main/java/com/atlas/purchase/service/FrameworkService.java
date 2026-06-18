package com.atlas.purchase.service;

import cn.hutool.core.util.IdUtil;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.entity.FrameworkAgreement;
import com.atlas.purchase.entity.FrameworkOrder;
import com.atlas.purchase.entity.FrameworkSupplier;
import com.atlas.purchase.mapper.FrameworkAgreementMapper;
import com.atlas.purchase.mapper.FrameworkOrderMapper;
import com.atlas.purchase.mapper.FrameworkSupplierMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 框架协议业务服务 / Framework agreement business service
 *
 * <p>完整生命周期：入围征集→评审→确定入围→协议生效→二次下单→履约跟踪。 /
 * Full lifecycle: supplier solicitation → evaluation → shortlisting → agreement activation → secondary ordering → fulfillment tracking.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FrameworkService {

    private final FrameworkAgreementMapper agreementMapper;
    private final FrameworkSupplierMapper supplierMapper;
    private final FrameworkOrderMapper orderMapper;

    /** 协议状态常量 / Agreement status constants */
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_COLLECTING = 1;
    public static final int STATUS_REVIEWING = 2;
    public static final int STATUS_SHORTLISTED = 3;
    public static final int STATUS_ACTIVE = 4;
    public static final int STATUS_EXECUTING = 5;
    public static final int STATUS_EXPIRED = 6;
    public static final int STATUS_TERMINATED = 7;

    /** 供应商状态常量 / Supplier status constants */
    public static final int SUPPLIER_SHORTLISTED = 1;
    public static final int SUPPLIER_SUSPENDED = 2;
    public static final int SUPPLIER_EXITED = 3;

    /** 订单状态常量 / Order status constants */
    public static final int ORDER_DRAFT = 0;
    public static final int ORDER_PLACED = 1;
    public static final int ORDER_CONFIRMED = 2;
    public static final int ORDER_FULFILLING = 3;
    public static final int ORDER_COMPLETED = 4;

    // ==================== 框架协议 / Framework Agreement ====================

    /**
     * 创建框架协议（草稿状态） / Create framework agreement (draft status)
     *
     * @param agreement 框架协议信息 / Framework agreement info
     * @return 创建成功的协议 / Created agreement
     */
    @Transactional(rollbackFor = Exception.class)
    public FrameworkAgreement createAgreement(FrameworkAgreement agreement) {
        agreement.setAgreementNo(generateNo("FA"));
        agreement.setStatus(STATUS_DRAFT);
        agreement.setActualSupplierCount(0);
        agreementMapper.insert(agreement);
        log.info("创建框架协议: no={} name={}", agreement.getAgreementNo(), agreement.getAgreementName());
        return agreement;
    }

    /**
     * 开启入围征集 / Start supplier solicitation
     *
     * @param agreementId 协议ID / Agreement ID
     * @return 更新后的协议 / Updated agreement
     */
    @Transactional(rollbackFor = Exception.class)
    public FrameworkAgreement startCollection(Long agreementId) {
        FrameworkAgreement agreement = getAgreement(agreementId);
        if (agreement.getStatus() != STATUS_DRAFT) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "当前状态不允许开启入围征集");
        }
        agreement.setStatus(STATUS_COLLECTING);
        agreementMapper.updateById(agreement);
        log.info("开启入围征集: no={}", agreement.getAgreementNo());
        return agreement;
    }

    /**
     * 入围评审 / Shortlisting evaluation
     *
     * @param agreementId 协议ID / Agreement ID
     * @return 更新后的协议 / Updated agreement
     */
    @Transactional(rollbackFor = Exception.class)
    public FrameworkAgreement review(Long agreementId) {
        FrameworkAgreement agreement = getAgreement(agreementId);
        if (agreement.getStatus() != STATUS_COLLECTING) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "当前状态不允许评审");
        }
        agreement.setStatus(STATUS_REVIEWING);
        agreementMapper.updateById(agreement);
        log.info("入围评审中: no={}", agreement.getAgreementNo());
        return agreement;
    }

    /**
     * 确定入围供应商并激活协议 / Confirm shortlisted suppliers and activate agreement
     *
     * @param agreementId 协议ID / Agreement ID
     * @return 更新后的协议 / Updated agreement
     */
    @Transactional(rollbackFor = Exception.class)
    public FrameworkAgreement confirmShortlist(Long agreementId) {
        FrameworkAgreement agreement = getAgreement(agreementId);
        if (agreement.getStatus() != STATUS_REVIEWING) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "当前状态不允许确定入围");
        }

        // 统计实际入围供应商数 / Count actual shortlisted suppliers
        long count = supplierMapper.selectCount(
                new LambdaQueryWrapper<FrameworkSupplier>()
                        .eq(FrameworkSupplier::getAgreementId, agreementId)
                        .eq(FrameworkSupplier::getStatus, SUPPLIER_SHORTLISTED));
        agreement.setActualSupplierCount((int) count);
        agreement.setStatus(STATUS_SHORTLISTED);
        agreementMapper.updateById(agreement);

        log.info("入围确定: no={} actualCount={}", agreement.getAgreementNo(), count);
        return agreement;
    }

    /**
     * 协议生效 / Activate agreement
     *
     * @param agreementId 协议ID / Agreement ID
     * @return 更新后的协议 / Updated agreement
     */
    @Transactional(rollbackFor = Exception.class)
    public FrameworkAgreement activate(Long agreementId) {
        FrameworkAgreement agreement = getAgreement(agreementId);
        if (agreement.getStatus() != STATUS_SHORTLISTED) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "当前状态不允许激活协议");
        }
        agreement.setStatus(STATUS_ACTIVE);
        agreementMapper.updateById(agreement);
        log.info("框架协议生效: no={}", agreement.getAgreementNo());
        return agreement;
    }

    // ==================== 供应商管理 / Supplier Management ====================

    /**
     * 添加入围供应商 / Add shortlisted supplier
     *
     * @param supplier 供应商信息 / Supplier info
     * @return 添加的供应商记录 / Added supplier record
     */
    @Transactional(rollbackFor = Exception.class)
    public FrameworkSupplier addSupplier(FrameworkSupplier supplier) {
        FrameworkAgreement agreement = getAgreement(supplier.getAgreementId());
        if (agreement.getStatus() < STATUS_COLLECTING) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "协议尚未开启入围征集");
        }
        supplier.setStatus(SUPPLIER_SHORTLISTED);
        if (supplier.getJoinedAt() == null) {
            supplier.setJoinedAt(LocalDateTime.now());
        }
        supplierMapper.insert(supplier);
        log.info("添加入围供应商: agreementId={} supplierId={}", supplier.getAgreementId(), supplier.getSupplierId());
        return supplier;
    }

    /**
     * 查询协议下所有入围供应商 / Query all shortlisted suppliers under agreement
     *
     * @param agreementId 协议ID / Agreement ID
     * @return 供应商列表 / Supplier list
     */
    public List<FrameworkSupplier> listSuppliers(Long agreementId) {
        return supplierMapper.selectList(
                new LambdaQueryWrapper<FrameworkSupplier>()
                        .eq(FrameworkSupplier::getAgreementId, agreementId)
                        .orderByAsc(FrameworkSupplier::getRank));
    }

    // ==================== 二次下单 / Secondary Order ====================

    /**
     * 创建二次订单 / Create secondary order
     *
     * @param order 二次订单信息 / Secondary order info
     * @return 创建的订单 / Created order
     */
    @Transactional(rollbackFor = Exception.class)
    public FrameworkOrder createOrder(FrameworkOrder order) {
        FrameworkAgreement agreement = getAgreement(order.getAgreementId());
        if (agreement.getStatus() < STATUS_ACTIVE) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "协议尚未生效，不可下单");
        }
        order.setOrderNo(generateNo("FO"));
        order.setStatus(ORDER_DRAFT);
        orderMapper.insert(order);

        // 更新协议为执行中 / Update agreement to executing
        if (agreement.getStatus() == STATUS_ACTIVE) {
            agreement.setStatus(STATUS_EXECUTING);
            agreementMapper.updateById(agreement);
        }

        log.info("创建二次订单: no={} agreementId={} amount={}",
                order.getOrderNo(), order.getAgreementId(), order.getOrderAmount());
        return order;
    }

    /**
     * 确认二次订单 / Confirm secondary order
     *
     * @param orderId 订单ID / Order ID
     * @return 更新后的订单 / Updated order
     */
    @Transactional(rollbackFor = Exception.class)
    public FrameworkOrder confirmOrder(Long orderId) {
        FrameworkOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_EXIST, "二次订单不存在");
        }
        if (order.getStatus() != ORDER_DRAFT && order.getStatus() != ORDER_PLACED) {
            throw new BizException(ErrorCode.ORDER_CANNOT_MODIFY, "当前状态不允许确认");
        }
        order.setStatus(ORDER_CONFIRMED);
        orderMapper.updateById(order);
        log.info("二次订单已确认: no={}", order.getOrderNo());
        return order;
    }

    /**
     * 完成二次订单履约 / Complete secondary order fulfillment
     *
     * @param orderId 订单ID / Order ID
     * @return 更新后的订单 / Updated order
     */
    @Transactional(rollbackFor = Exception.class)
    public FrameworkOrder completeOrder(Long orderId) {
        FrameworkOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException(ErrorCode.ORDER_NOT_EXIST, "二次订单不存在");
        }
        order.setStatus(ORDER_COMPLETED);
        orderMapper.updateById(order);
        log.info("二次订单履约完成: no={}", order.getOrderNo());
        return order;
    }

    // ==================== 查询 / Query ====================

    /**
     * 分页查询框架协议（支持 keyword + status 筛选） /
     * Paginated query of framework agreements (supports keyword + status filtering)
     *
     * @param keyword 关键字 / Keyword
     * @param status  状态（可选） / Status (optional)
     * @param page    当前页 / Current page
     * @param size    每页大小 / Page size
     * @return 分页结果 / Paginated result
     */
    public Page<FrameworkAgreement> pageAgreements(String keyword, Integer status, int page, int size) {
        LambdaQueryWrapper<FrameworkAgreement> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(FrameworkAgreement::getAgreementNo, keyword)
                    .or().like(FrameworkAgreement::getAgreementName, keyword));
        }
        if (status != null) {
            wrapper.eq(FrameworkAgreement::getStatus, status);
        }
        wrapper.orderByDesc(FrameworkAgreement::getCreatedAt);
        return agreementMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 查询框架协议详情 / Query framework agreement detail
     *
     * @param agreementId 协议ID / Agreement ID
     * @return 框架协议 / Framework agreement
     */
    public FrameworkAgreement getAgreement(Long agreementId) {
        FrameworkAgreement agreement = agreementMapper.selectById(agreementId);
        if (agreement == null) {
            throw new BizException(ErrorCode.ORDER_NOT_EXIST, "框架协议不存在");
        }
        return agreement;
    }

    /**
     * 分页查询二次订单 / Paginated query of secondary orders
     *
     * @param agreementId 协议ID（可选） / Agreement ID (optional)
     * @param status      状态（可选） / Status (optional)
     * @param page        当前页 / Current page
     * @param size        每页大小 / Page size
     * @return 分页结果 / Paginated result
     */
    public Page<FrameworkOrder> pageOrders(Long agreementId, Integer status, int page, int size) {
        LambdaQueryWrapper<FrameworkOrder> wrapper = new LambdaQueryWrapper<>();
        if (agreementId != null) {
            wrapper.eq(FrameworkOrder::getAgreementId, agreementId);
        }
        if (status != null) {
            wrapper.eq(FrameworkOrder::getStatus, status);
        }
        wrapper.orderByDesc(FrameworkOrder::getCreatedAt);
        return orderMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /** 生成编号：FA/FO + 日期 + 随机串 / Generate number: FA/FO + date + random string */
    private String generateNo(String prefix) {
        return prefix + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + IdUtil.fastSimpleUUID().substring(0, 6).toUpperCase();
    }
}
