package com.atlas.contract.service;

import com.atlas.contract.entity.Contract;
import com.atlas.contract.entity.ContractChangeLog;
import com.atlas.contract.mapper.ContractChangeLogMapper;
import com.atlas.contract.mapper.ContractMapper;
import com.atlas.contract.state.ContractStatusEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 合同服务 — CRUD + 状态机流转 + 变更日志 /
 * Contract service — CRUD + state machine transitions + change log
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractService {

    private final ContractMapper contractMapper;
    private final ContractChangeLogMapper changeLogMapper;

    // ============ CRUD ============

    public Page<Contract> page(String keyword, Integer status, int page, int size) {
        LambdaQueryWrapper<Contract> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Contract::getContractName, keyword)
                   .or().like(Contract::getContractNo, keyword);
        }
        if (status != null) {
            wrapper.eq(Contract::getStatus, status);
        }
        wrapper.orderByDesc(Contract::getCreatedAt);
        return contractMapper.selectPage(new Page<>(page, size), wrapper);
    }

    public Contract getById(Long id) {
        return contractMapper.selectById(id);
    }

    @Transactional
    public boolean save(Contract contract) {
        contract.setStatus(ContractStatusEnum.DRAFT.getCode());
        return contractMapper.insert(contract) > 0;
    }

    @Transactional
    public boolean update(Contract contract) {
        Contract old = contractMapper.selectById(contract.getId());
        if (old == null) return false;
        // 简单字段更新，不触发状态机 / Simple field update, no state machine trigger
        return contractMapper.updateById(contract) > 0;
    }

    // ============ 状态机流转 / State Machine Transitions ============

    /**
     * 状态流转 — 带合法性校验 + 变更日志记录 /
     * State transition — with legality validation + change log recording
     */
    @Transactional
    public boolean transition(Long contractId, int targetStatus, Long operatorId, String operatorName) {
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) {
            throw new IllegalArgumentException("合同不存在: " + contractId);
        }

        // 状态机校验 / State machine validation
        ContractStatusEnum.validateTransition(contract.getStatus(), targetStatus);

        String fromDesc = ContractStatusEnum.of(contract.getStatus()).getDesc();
        String toDesc = ContractStatusEnum.of(targetStatus).getDesc();

        // 更新状态 / Update status
        contract.setStatus(targetStatus);
        contractMapper.updateById(contract);

        // 记录变更日志 / Record change log
        ContractChangeLog changeLog = new ContractChangeLog();
        changeLog.setContractId(contractId);
        changeLog.setFieldName("status");
        changeLog.setOldValue(fromDesc);
        changeLog.setNewValue(toDesc);
        changeLog.setOperatorId(operatorId);
        changeLog.setOperatorName(operatorName);
        changeLogMapper.insert(changeLog);

        log.info("合同 {} 状态变更: {} -> {}", contract.getContractNo(), fromDesc, toDesc);
        return true;
    }

    /**
     * 驳回 — 可附带驳回原因 / Reject — with optional rejection reason
     */
    @Transactional
    public boolean reject(Long contractId, String reason, Long operatorId, String operatorName) {
        Contract contract = contractMapper.selectById(contractId);
        if (contract == null) return false;
        contract.setRejectReason(reason);
        contractMapper.updateById(contract);
        return transition(contractId, ContractStatusEnum.REJECTED.getCode(), operatorId, operatorName);
    }
}
