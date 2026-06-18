package com.atlas.contract.econtract.service;

import com.atlas.contract.econtract.dto.ClauseDiffResponse;
import com.atlas.contract.econtract.mapper.CntClauseCompareMapper;
import com.atlas.contract.econtract.model.CntClauseCompare;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 条款比对服务 — 两版合同文本 diff，输出 JSON 格式差异结果 /
 * Clause compare service — diff two versions of contract text, output JSON format diff result
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClauseCompareService extends ServiceImpl<CntClauseCompareMapper, CntClauseCompare> {

    private final CntClauseCompareMapper compareMapper;

    /**
     * 条款版本比对 /
     * Clause version comparison
     *
     * @param contractId    合同ID / Contract ID
     * @param sourceVersion 原始版本号 / Source version
     * @param targetVersion 对比目标版本号 / Target version
     * @param sourceText    原始合同文本 / Source contract text
     * @param targetText    目标合同文本 / Target contract text
     * @param comparedBy    比对人 / Compared by
     * @return 比对结果响应 / Comparison result response
     */
    @Transactional(rollbackFor = Exception.class)
    public ClauseDiffResponse compare(Long contractId, String sourceVersion, String targetVersion,
                                       String sourceText, String targetText, String comparedBy) {
        String diffResult = computeDiff(sourceText, targetText);

        CntClauseCompare compare = new CntClauseCompare();
        compare.setContractId(contractId);
        compare.setSourceVersion(sourceVersion);
        compare.setTargetVersion(targetVersion);
        compare.setDiffResult(diffResult);
        compare.setComparedBy(comparedBy);
        compare.setComparedAt(LocalDateTime.now());
        save(compare);

        log.info("条款比对完成: contractId={} {} -> {}  comparedBy={}",
                contractId, sourceVersion, targetVersion, comparedBy);

        return ClauseDiffResponse.builder()
                .compareId(compare.getId())
                .contractId(contractId)
                .sourceVersion(sourceVersion)
                .targetVersion(targetVersion)
                .diffResult(diffResult)
                .comparedBy(comparedBy)
                .comparedAt(compare.getComparedAt())
                .build();
    }

    /**
     * 查询合同的比对历史 / Query comparison history by contract ID
     */
    public java.util.List<CntClauseCompare> listByContractId(Long contractId) {
        LambdaQueryWrapper<CntClauseCompare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CntClauseCompare::getContractId, contractId)
               .orderByDesc(CntClauseCompare::getComparedAt);
        return compareMapper.selectList(wrapper);
    }

    // ============ Diff 算法 / Diff Algorithm ============

    /**
     * 计算两段文本的差异，输出 JSON 格式结果 /
     * Compute diff of two text blocks, output JSON format result
     *
     * <p>基于行级 LCS (Longest Common Subsequence) 算法实现 /
     * Based on line-level LCS (Longest Common Subsequence) algorithm
     */
    private String computeDiff(String sourceText, String targetText) {
        String[] sourceLines = sourceText != null ? sourceText.split("\n") : new String[0];
        String[] targetLines = targetText != null ? targetText.split("\n") : new String[0];

        StringBuilder json = new StringBuilder("{\n");
        json.append("  \"totalChanges\": 0,\n");
        json.append("  \"addedLines\": 0,\n");
        json.append("  \"removedLines\": 0,\n");
        json.append("  \"modifiedLines\": 0,\n");
        json.append("  \"changes\": [\n");

        int m = sourceLines.length;
        int n = targetLines.length;
        int[] maxLen = {0};  // mutable for lambda

        // Build LCS table / 构建 LCS 表
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (sourceLines[i - 1].equals(targetLines[j - 1])) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    if (dp[i][j] > maxLen[0]) maxLen[0] = dp[i][j];
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        // Reconstruct diff by backtracking LCS / 回溯 LCS 表形构差异
        java.util.List<String> changes = new java.util.ArrayList<>();
        int[] stats = {0, 0, 0}; // added, removed, modified
        int[] totalChanges = {0};

        // Use simple line-by-line diff for readability
        int si = 0, ti = 0;
        while (si < m && ti < n) {
            if (sourceLines[si].equals(targetLines[ti])) {
                si++;
                ti++;
            } else {
                // Check if line was modified (replaced with different text)
                boolean foundInTarget = false;
                for (int k = ti + 1; k < Math.min(ti + 5, n); k++) {
                    if (sourceLines[si].equals(targetLines[k])) {
                        // Lines ti..k-1 were added
                        for (int a = ti; a < k; a++) {
                            changes.add(String.format(
                                "    {\"type\":\"ADDED\",\"line\":%d,\"content\":%s}",
                                a + 1, escJson(targetLines[a])));
                            stats[0]++;
                            totalChanges[0]++;
                        }
                        ti = k;
                        foundInTarget = true;
                        break;
                    }
                }
                if (!foundInTarget) {
                    boolean foundInSource = false;
                    for (int k = si + 1; k < Math.min(si + 5, m); k++) {
                        if (targetLines[ti].equals(sourceLines[k])) {
                            // Lines si..k-1 were removed
                            for (int r = si; r < k; r++) {
                                changes.add(String.format(
                                    "    {\"type\":\"REMOVED\",\"line\":%d,\"content\":%s}",
                                    r + 1, escJson(sourceLines[r])));
                                stats[1]++;
                                totalChanges[0]++;
                            }
                            si = k;
                            foundInSource = true;
                            break;
                        }
                    }
                    if (!foundInSource) {
                        // Modified line
                        changes.add(String.format(
                            "    {\"type\":\"MODIFIED\",\"line\":%d,\"oldContent\":%s,\"newContent\":%s}",
                            si + 1, escJson(sourceLines[si]), escJson(targetLines[ti])));
                        stats[2]++;
                        totalChanges[0]++;
                        si++;
                        ti++;
                    }
                }
            }
        }

        // Remaining lines in source = removed
        while (si < m) {
            changes.add(String.format(
                "    {\"type\":\"REMOVED\",\"line\":%d,\"content\":%s}",
                si + 1, escJson(sourceLines[si])));
            stats[1]++;
            totalChanges[0]++;
            si++;
        }

        // Remaining lines in target = added
        while (ti < n) {
            changes.add(String.format(
                "    {\"type\":\"ADDED\",\"line\":%d,\"content\":%s}",
                ti + 1, escJson(targetLines[ti])));
            stats[0]++;
            totalChanges[0]++;
            ti++;
        }

        json.append(String.join(",\n", changes));
        json.append("\n  ]\n}");

        // Update totals in JSON
        String result = json.toString();
        result = result.replace("\"totalChanges\": 0", "\"totalChanges\": " + totalChanges[0]);
        result = result.replace("\"addedLines\": 0", "\"addedLines\": " + stats[0]);
        result = result.replace("\"removedLines\": 0", "\"removedLines\": " + stats[1]);
        result = result.replace("\"modifiedLines\": 0", "\"modifiedLines\": " + stats[2]);

        return result;
    }

    /**
     * JSON 字符串转义 / JSON string escape
     */
    private String escJson(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\")
                       .replace("\"", "\\\"")
                       .replace("\n", "\\n")
                       .replace("\r", "\\r")
                       .replace("\t", "\\t") + "\"";
    }
}
