package com.atlas.supplier.service.portal;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.config.SupplierSecurityConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 合同服务（供应商端） — 供应商视角：查看合同、在线签署、履约查询、合同预警 /
 * Contract service (portal) — supplier perspective: view contracts, online signing, performance tracking, contract alerts
 *
 * <p>复用 atlas-contract 模块的 Contract 实体和 Mapper，通过 supplier_id 做数据隔离。 /
 * Reuses Contract entity and Mapper from atlas-contract module, with supplier_id data isolation.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalContractService {

    // 注意：以下 Mapper 来自 atlas-contract，实际开发需添加模块依赖或使用 Feign 调用 /
    // Note: The following Mappers are from atlas-contract; in practice add module dependency or use Feign
    // private final ContractMapper contractMapper;
    // private final CntSignFlowMapper signFlowMapper;
    // private final CntSignRecordMapper signRecordMapper;
    // private final CntPerformanceMapper performanceMapper;
    // private final CntPerformanceAlertMapper alertMapper;

    /**
     * 查看企业发来的合同列表（分页、状态筛选） /
     * View contract list from enterprises (paginated, status filter)
     *
     * @param page   页码 / Page number
     * @param size   每页条数 / Page size
     * @param status 状态筛选（可选: DRAFT / SIGNING / COMPLETED / TERMINATED）/ Status filter (optional)
     * @return 分页结果 / Paginated result
     */
    public Page<Map<String, Object>> listContracts(int page, int size, String status) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        log.info("查询合同列表: supplierId={}, status={}", supplierId, status);
        // LambdaQueryWrapper<Contract> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq(Contract::getSupplierId, supplierId);
        // if (StringUtils.hasText(status)) {
        //     wrapper.eq(Contract::getStatus, status);
        // }
        // wrapper.orderByDesc(Contract::getCreatedAt);
        // return contractMapper.selectPage(new Page<>(page, size), wrapper);
        return new Page<>(page, size);
    }

    /**
     * 查看合同详情（条款、金额、履约要求） /
     * View contract detail (terms, amount, performance requirements)
     *
     * @param contractId 合同ID / Contract ID
     * @return 合同详情 / Contract detail
     */
    public Map<String, Object> getContractDetail(Long contractId) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        // 验证供应商权限 / Validate supplier permission
        // Contract contract = contractMapper.selectById(contractId);
        // if (contract == null || !contract.getSupplierId().equals(supplierId)) {
        //     throw new BizException(ErrorCode.FORBIDDEN, "无权查看该合同 / Not authorized to view this contract");
        // }

        log.info("查看合同详情: contractId={}, supplierId={}", contractId, supplierId);

        Map<String, Object> result = new HashMap<>();
        result.put("contractId", contractId);
        result.put("message", "合同详情功能需集成 atlas-contract 模块 / Contract detail requires atlas-contract integration");
        return result;
    }

    /**
     * 在线签署合同（二次确认 + SMS 验证） /
     * Online contract signing (double confirmation + SMS verification)
     *
     * @param contractId 合同ID / Contract ID
     * @param smsCode    SMS 验证码 / SMS verification code
     */
    public void signContract(Long contractId, String smsCode) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        // 验证 SMS 验证码 / Validate SMS verification code
        // if (!smsService.verifyCode(supplierPhone, smsCode)) {
        //     throw new BizException(ErrorCode.BAD_REQUEST, "短信验证码错误 / SMS verification code is incorrect");
        // }

        // 状态机校验：只有 SIGNING 状态可签署 / State machine validation: only SIGNING status can be signed
        // CntSignFlow flow = signFlowMapper.selectByContractId(contractId);
        // if (!"SIGNING".equals(flow.getStatus())) {
        //     throw new BizException(ErrorCode.BAD_REQUEST, "当前合同状态不允许签署 / Current contract status does not allow signing");
        // }

        // 创建签署记录 / Create signing record
        // CntSignRecord record = new CntSignRecord();
        // record.setFlowId(flow.getId());
        // record.setSignerType("EXTERNAL");
        // record.setSignerId(supplierId);
        // record.setSignStatus("SIGNED");
        // signRecordMapper.insert(record);

        log.info("供应商在线签署合同: contractId={}, supplierId={}", contractId, supplierId);
    }

    /**
     * 拒绝签署（含原因） / Reject signing (with reason)
     *
     * @param contractId   合同ID / Contract ID
     * @param rejectReason 拒绝原因 / Reject reason
     */
    public void rejectContract(Long contractId, String rejectReason) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        // 创建拒绝签署记录 / Create rejection signing record
        // CntSignRecord record = new CntSignRecord();
        // record.setFlowId(flowId);
        // record.setSignerType("EXTERNAL");
        // record.setSignerId(supplierId);
        // record.setSignStatus("REJECTED");
        // record.setRejectReason(rejectReason);
        // signRecordMapper.insert(record);

        log.info("供应商拒绝签署合同: contractId={}, supplierId={}, reason={}", contractId, supplierId, rejectReason);
    }

    /**
     * 查看履约进度（交付/付款/质量指标） /
     * View performance progress (delivery/payment/quality metrics)
     *
     * @param contractId 合同ID / Contract ID
     * @return 履约进度列表 / Performance progress list
     */
    public List<Map<String, Object>> getPerformance(Long contractId) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        log.info("查看履约进度: contractId={}, supplierId={}", contractId, supplierId);
        // return performanceMapper.selectList(
        //     new LambdaQueryWrapper<CntPerformance>()
        //         .eq(CntPerformance::getContractId, contractId)
        //         .orderByAsc(CntPerformance::getDueDate)
        // );
        return List.of();
    }

    /**
     * 查看合同预警（到期/违约提醒） / View contract alerts (expiry/breach notifications)
     *
     * @param contractId 合同ID / Contract ID
     * @return 预警列表 / Alert list
     */
    public List<Map<String, Object>> getContractAlerts(Long contractId) {
        Long supplierId = SupplierSecurityConfig.getCurrentSupplierId();

        log.info("查看合同预警: contractId={}, supplierId={}", contractId, supplierId);
        // return alertMapper.selectList(
        //     new LambdaQueryWrapper<CntPerformanceAlert>()
        //         .eq(CntPerformanceAlert::getContractId, contractId)
        //         .eq(CntPerformanceAlert::getIsSent, 1)
        //         .orderByDesc(CntPerformanceAlert::getCreatedAt)
        // );
        return List.of();
    }
}
