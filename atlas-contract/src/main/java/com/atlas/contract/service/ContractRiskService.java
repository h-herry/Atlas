package com.atlas.contract.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.contract.entity.ContractRiskClause;
import com.atlas.contract.mapper.ContractRiskClauseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 合同风险预警 Service / Contract risk alert Service
 *
 * <p>通过预设风险关键词自动匹配合同条款中的风险项，支持人工标注和审批流联动。
 * 风险类型：PRICE(价格风险)、DELIVERY(交付风险)、PAYMENT(付款风险)、LEGAL(法律风险)。 /
 * Auto-matches risk items in contract clauses via preset keywords, supports manual annotation and approval workflow linkage.
 * Risk types: PRICE, DELIVERY, PAYMENT, LEGAL.
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractRiskService extends ServiceImpl<ContractRiskClauseMapper, ContractRiskClause> {

    private final ContractRiskClauseMapper riskMapper;

    /** 风险关键词库 / Risk keyword dictionary */
    private static final Map<String, String> RISK_KEYWORDS = new LinkedHashMap<>();
    static {
        RISK_KEYWORDS.put("价格波动", "PRICE");
        RISK_KEYWORDS.put("价格上涨", "PRICE");
        RISK_KEYWORDS.put("价格调整不以", "PRICE");
        RISK_KEYWORDS.put("逾期交付", "DELIVERY");
        RISK_KEYWORDS.put("延迟交货", "DELIVERY");
        RISK_KEYWORDS.put("不承担延迟", "DELIVERY");
        RISK_KEYWORDS.put("验收标准不明确", "DELIVERY");
        RISK_KEYWORDS.put("预付全款", "PAYMENT");
        RISK_KEYWORDS.put("无条件付款", "PAYMENT");
        RISK_KEYWORDS.put("付款周期超过", "PAYMENT");
        RISK_KEYWORDS.put("单方解除", "LEGAL");
        RISK_KEYWORDS.put("不承担赔偿责任", "LEGAL");
        RISK_KEYWORDS.put("争议由对方所在地", "LEGAL");
        RISK_KEYWORDS.put("罚款金额超过", "LEGAL");
    }

    /**
     * 按合同ID查询风险条款 / Query risk clauses by contract ID
     */
    public List<ContractRiskClause> listByContractId(Long contractId) {
        LambdaQueryWrapper<ContractRiskClause> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContractRiskClause::getContractId, contractId);
        return riskMapper.selectList(wrapper);
    }

    /**
     * 按风险等级查询 / Query by risk level
     */
    public List<ContractRiskClause> listByRiskLevel(String riskLevel) {
        LambdaQueryWrapper<ContractRiskClause> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContractRiskClause::getRiskLevel, riskLevel);
        return riskMapper.selectList(wrapper);
    }

    /**
     * 扫描合同全文，自动标记风险条款 / Scan full contract text and auto-mark risk clauses
     *
     * @param contractId 合同ID / Contract ID
     * @param fullText   合同全文 / Full contract text
     * @return 识别出的风险条款数量 / Number of risk clauses identified
     */
    @Transactional(rollbackFor = Exception.class)
    public int scanRisk(Long contractId, String fullText) {
        int count = 0;
        for (Map.Entry<String, String> entry : RISK_KEYWORDS.entrySet()) {
            String keyword = entry.getKey();
            String riskType = entry.getValue();
            if (fullText.contains(keyword)) {
                ContractRiskClause clause = new ContractRiskClause();
                clause.setContractId(contractId);
                clause.setClauseContent(extractClauseAround(fullText, keyword));
                clause.setRiskType(riskType);
                clause.setRiskLevel("MEDIUM");
                clause.setSuggestion("建议关注该条款，必要时协商修改");
                save(clause);
                count++;
            }
        }
        log.info("合同风险扫描完成: contractId={} 识别风险条款 {} 条", contractId, count);
        return count;
    }

    /**
     * 人工标注风险条款 / Manually annotate risk clause
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean manualMark(Long contractId, String clauseContent, String riskType,
                               String riskLevel, String suggestion) {
        ContractRiskClause clause = new ContractRiskClause();
        clause.setContractId(contractId);
        clause.setClauseContent(clauseContent);
        clause.setRiskType(riskType);
        clause.setRiskLevel(riskLevel);
        clause.setSuggestion(suggestion);
        return save(clause);
    }

    /**
     * 更新风险等级/建议 / Update risk level / suggestion
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRiskLevel(Long id, String riskLevel, String suggestion) {
        ContractRiskClause clause = getById(id);
        if (clause == null) {
            throw new BizException(ErrorCode.RISK_CLAUSE_NOT_EXIST);
        }
        clause.setRiskLevel(riskLevel);
        if (suggestion != null) {
            clause.setSuggestion(suggestion);
        }
        return updateById(clause);
    }

    /**
     * 提取关键词所在的上下文（前后50字） / Extract context around keyword (±50 chars)
     */
    private String extractClauseAround(String fullText, String keyword) {
        int idx = fullText.indexOf(keyword);
        if (idx < 0) return keyword;
        int start = Math.max(0, idx - 50);
        int end = Math.min(fullText.length(), idx + keyword.length() + 50);
        return fullText.substring(start, end);
    }
}
