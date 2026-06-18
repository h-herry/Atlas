package com.atlas.contract.econtract.service;

import com.atlas.contract.econtract.dto.SignFlowCreateRequest;
import com.atlas.contract.econtract.dto.SignFlowStatusResponse;
import com.atlas.contract.econtract.enums.SignStatus;
import com.atlas.contract.econtract.mapper.CntSignFlowMapper;
import com.atlas.contract.econtract.mapper.CntSignRecordMapper;
import com.atlas.contract.econtract.model.CntSignFlow;
import com.atlas.contract.econtract.model.CntSignRecord;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 签署流程服务 — 流程管理 + 状态机流转 + 签署/拒签 /
 * Sign flow service — flow management + state machine transitions + sign/reject
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignFlowService extends ServiceImpl<CntSignFlowMapper, CntSignFlow> {

    private final CntSignFlowMapper flowMapper;
    private final CntSignRecordMapper recordMapper;

    // ============ 发起签署流程 / Initiate Sign Flow ============

    /**
     * 发起签署流程 / Initiate sign flow
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createFlow(SignFlowCreateRequest request, Long initiatorId) {
        CntSignFlow flow = new CntSignFlow();
        flow.setContractId(request.getContractId());
        flow.setSignType(request.getSignType());
        flow.setStatus(SignStatus.DRAFT.getCode());
        flow.setInitiatorId(initiatorId);
        flow.setSignDeadline(request.getSignDeadline());

        // 自动计算步骤数 / Auto-calculate step count
        List<SignFlowCreateRequest.SignerInfo> signers = request.getSigners();
        flow.setTotalSteps(signers.size());
        flow.setCurrentStep(1);

        flowMapper.insert(flow);

        // 批量创建签署记录 / Batch create sign records
        for (int i = 0; i < signers.size(); i++) {
            SignFlowCreateRequest.SignerInfo info = signers.get(i);
            CntSignRecord record = new CntSignRecord();
            record.setFlowId(flow.getId());
            record.setStepOrder(i + 1);
            record.setSignerType(info.getSignerType());
            record.setSignerId(info.getSignerId());
            record.setSignerName(info.getSignerName());
            record.setSignerCompany(info.getSignerCompany());
            record.setSignMethod(info.getSignMethod());
            record.setSignStatus("PENDING");
            recordMapper.insert(record);
        }

        // 转为 SIGNING 状态 / Transition to SIGNING status
        transitionStatus(flow, SignStatus.SIGNING.getCode());

        log.info("签署流程已发起: flowId={} contractId={} 共{}个签署步骤",
                flow.getId(), request.getContractId(), signers.size());
        return flow.getId();
    }

    // ============ 查看签署进度 / View Sign Progress ============

    /**
     * 查看签署进度 / View sign progress
     */
    public SignFlowStatusResponse getFlowStatus(Long flowId) {
        CntSignFlow flow = flowMapper.selectById(flowId);
        if (flow == null) {
            throw new IllegalArgumentException("签署流程不存在: " + flowId);
        }

        LambdaQueryWrapper<CntSignRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CntSignRecord::getFlowId, flowId)
               .orderByAsc(CntSignRecord::getStepOrder);
        List<CntSignRecord> records = recordMapper.selectList(wrapper);

        List<SignFlowStatusResponse.SignRecordItem> recordItems = records.stream()
                .map(r -> SignFlowStatusResponse.SignRecordItem.builder()
                        .stepOrder(r.getStepOrder())
                        .signerName(r.getSignerName())
                        .signerCompany(r.getSignerCompany())
                        .signStatus(r.getSignStatus())
                        .signedAt(r.getSignedAt())
                        .signMethod(r.getSignMethod())
                        .rejectReason(r.getRejectReason())
                        .build())
                .collect(Collectors.toList());

        return SignFlowStatusResponse.builder()
                .flowId(flow.getId())
                .contractId(flow.getContractId())
                .signType(flow.getSignType())
                .status(flow.getStatus())
                .currentStep(flow.getCurrentStep())
                .totalSteps(flow.getTotalSteps())
                .signDeadline(flow.getSignDeadline())
                .records(recordItems)
                .build();
    }

    // ============ 在线签署 / Online Sign ============

    /**
     * 在线签署 / Online sign
     */
    @Transactional(rollbackFor = Exception.class)
    public void sign(Long flowId, Long signerId, String signIp, String signMethod) {
        CntSignFlow flow = flowMapper.selectById(flowId);
        if (flow == null) {
            throw new IllegalArgumentException("签署流程不存在: " + flowId);
        }
        if (!SignStatus.SIGNING.getCode().equals(flow.getStatus())) {
            throw new IllegalStateException("当前签署状态不允许签署: " + flow.getStatus());
        }

        // 查找当前步骤对应的签署记录 / Find sign record for current step
        CntSignRecord record = findPendingRecord(flowId, flow.getCurrentStep());
        if (record == null) {
            throw new IllegalStateException("未找到当前步骤的待签署记录");
        }
        if (!record.getSignerId().equals(signerId)) {
            throw new IllegalStateException("当前签署人不匹配: 需要 " + record.getSignerName());
        }

        record.setSignStatus("SIGNED");
        record.setSignedAt(LocalDateTime.now());
        record.setSignIp(signIp);
        record.setSignMethod(signMethod);
        recordMapper.updateById(record);

        // 推进到下一步或完成 / Advance to next step or complete
        if (flow.getCurrentStep() < flow.getTotalSteps()) {
            flow.setCurrentStep(flow.getCurrentStep() + 1);
            flowMapper.updateById(flow);
            log.info("签署流程推进: flowId={} step={}/{}", flowId, flow.getCurrentStep(), flow.getTotalSteps());
        } else {
            transitionStatus(flow, SignStatus.COMPLETED.getCode());
            flow.setCompletedAt(LocalDateTime.now());
            flowMapper.updateById(flow);
            log.info("签署流程完成: flowId={}", flowId);
        }
    }

    // ============ 拒绝签署 / Reject Sign ============

    /**
     * 拒绝签署 / Reject sign
     */
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long flowId, Long signerId, String reason) {
        CntSignFlow flow = flowMapper.selectById(flowId);
        if (flow == null) {
            throw new IllegalArgumentException("签署流程不存在: " + flowId);
        }

        CntSignRecord record = findPendingRecord(flowId, flow.getCurrentStep());
        if (record == null) {
            throw new IllegalStateException("未找到当前步骤的待签署记录");
        }

        record.setSignStatus("REJECTED");
        record.setRejectReason(reason);
        recordMapper.updateById(record);

        log.info("签署被拒绝: flowId={} signer={} reason={}", flowId, signerId, reason);
    }

    // ============ 查询签署记录 / Query Sign Records ============

    /**
     * 查询合同的所有签署记录 / Query all sign records by contract ID
     */
    public List<CntSignRecord> listRecordsByContractId(Long contractId) {
        // 先查该合同的所有签署流程 / Find all sign flows for the contract
        LambdaQueryWrapper<CntSignFlow> flowWrapper = new LambdaQueryWrapper<>();
        flowWrapper.eq(CntSignFlow::getContractId, contractId);
        List<CntSignFlow> flows = flowMapper.selectList(flowWrapper);

        if (flows.isEmpty()) {
            return List.of();
        }

        // 合并所有流程的签署记录 / Merge sign records from all flows
        List<Long> flowIds = flows.stream().map(CntSignFlow::getId).collect(Collectors.toList());
        LambdaQueryWrapper<CntSignRecord> recordWrapper = new LambdaQueryWrapper<>();
        recordWrapper.in(CntSignRecord::getFlowId, flowIds)
                     .orderByAsc(CntSignRecord::getStepOrder);
        return recordMapper.selectList(recordWrapper);
    }

    // ============ 内部工具方法 / Internal Utility Methods ============

    /**
     * 状态流转 — 带合法性校验 / State transition — with legality validation
     */
    private void transitionStatus(CntSignFlow flow, String targetStatus) {
        SignStatus.validateTransition(flow.getStatus(), targetStatus);
        flow.setStatus(targetStatus);
        flowMapper.updateById(flow);
    }

    /**
     * 查找指定步骤的待签署记录 / Find pending record for specified step
     */
    private CntSignRecord findPendingRecord(Long flowId, int stepOrder) {
        LambdaQueryWrapper<CntSignRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CntSignRecord::getFlowId, flowId)
               .eq(CntSignRecord::getStepOrder, stepOrder)
               .eq(CntSignRecord::getSignStatus, "PENDING");
        return recordMapper.selectOne(wrapper);
    }
}
