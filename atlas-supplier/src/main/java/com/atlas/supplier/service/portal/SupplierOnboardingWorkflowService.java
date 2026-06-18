package com.atlas.supplier.service.portal;

import com.alibaba.fastjson2.JSON;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.dto.portal.*;
import com.atlas.supplier.entity.OnboardingApproval;
import com.atlas.supplier.entity.PortalRegister;
import com.atlas.supplier.entity.Supplier;
import com.atlas.supplier.mapper.OnboardingApprovalMapper;
import com.atlas.supplier.mapper.PortalRegisterMapper;
import com.atlas.supplier.mapper.SupplierMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.*;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 供应商入驻审批流服务 — Flowable BPMN 审批流程 /
 * Supplier onboarding workflow service — Flowable BPMN approval process
 *
 * <p>审批节点 / Approval nodes:
 * 开始 → 资质初审（采购主管）→ 现场考察（可选，品控）→ 终审（采购总监）→ 通过/驳回 /
 * Start → Initial Review (Purchase Supervisor) → Site Inspection (Optional, QC) → Final Review (Purchase Director) → Approved/Rejected</p>
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierOnboardingWorkflowService {

    private final PortalRegisterMapper portalRegisterMapper;
    private final OnboardingApprovalMapper approvalMapper;
    private final SupplierMapper supplierMapper;

    /**
     * Flowable 流程引擎 — Spring Boot 自动装配，若未集成则本字段为 null /
     * Flowable process engine — Spring Boot auto-configuration; null if not integrated
     */
    @Autowired(required = false)
    private ProcessEngine processEngine;

    /** 流程定义 Key — BPMN 部署时使用 / Process definition key — used in BPMN deployment */
    private static final String PROCESS_DEFINITION_KEY = "supplier_onboarding";

    // ==================== 启动审批流 / Start Approval Process ====================

    /**
     * 启动入驻审批流程 /
     * Start onboarding approval process
     *
     * @param application   入驻申请实体 / Onboarding application entity
     * @param initiatorType 发起人类型：SELF（自助）或 PURCHASER（采购员代注册） / Initiator type: SELF=self-registration or PURCHASER=proxy
     * @return 流程实例ID / Process instance ID (null if Flowable not integrated)
     */
    @Transactional(rollbackFor = Exception.class)
    public String startApprovalProcess(PortalRegister application, String initiatorType) {
        if (processEngine == null) {
            log.warn("Flowable未集成，审批流程将以本地状态机模式运行 / Flowable not integrated, approval will run in local state-machine mode");
            return null;
        }

        try {
            RuntimeService runtimeService = processEngine.getRuntimeService();

            // 构建流程变量 / Build process variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("applyId", application.getId());
            variables.put("companyName", application.getCompanyName());
            variables.put("contactPhone", application.getContactPhone());
            variables.put("contactEmail", application.getContactEmail());
            variables.put("initiatorType", initiatorType);
            variables.put("initiatorId", application.getInitiatorId());
            variables.put("initiatorName", application.getInitiatorName());

            // 启动流程实例 / Start process instance
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                    PROCESS_DEFINITION_KEY,
                    String.valueOf(application.getId()),
                    variables
            );

            String processInstanceId = processInstance.getProcessInstanceId();

            // 回写流程实例ID / Write back process instance ID
            application.setProcessInstanceId(processInstanceId);
            application.setApplyStatus(1); // 审批中 / In review
            portalRegisterMapper.updateById(application);

            // 发送通知给审批人（资质初审 — 采购主管） / Send notification to approver (Initial Review — Purchase Supervisor)
            sendApprovalNotification(application, "INITIAL_REVIEW");

            log.info("入驻审批流程已启动: applyId={}, processInstanceId={}, initiatorType={}",
                    application.getId(), processInstanceId, initiatorType);

            return processInstanceId;

        } catch (Exception e) {
            log.error("启动审批流程失败: applyId={}, error={}", application.getId(), e.getMessage(), e);
            // 流程启动失败不阻塞申请提交，降级为本地状态机 / Process startup failure does not block application submission, fallback to local state machine
            // 注意：此 catch 有意不重新抛出异常。Flowable 是外部系统无法事务回滚，
            // 降级模式不影响数据一致性，事务正常提交以保留本地状态 / 
            // Note: Intentionally not re-throwing. Flowable is an external system with no transaction rollback;
            // the fallback mode does not compromise data consistency; transaction commits to preserve local state.
            application.setApplyStatus(0); // 待审批 / Pending
            portalRegisterMapper.updateById(application);
            return null;
        }
    }

    // ==================== 审批步骤 / Approval Steps ====================

    /**
     * 执行审批步骤 — 通过或驳回 /
     * Execute approval step — approve or reject
     *
     * @param taskId   Flowable 任务ID / Flowable task ID
     * @param approved 是否通过 / Whether approved
     * @param comment  审批意见 / Comment
     */
    @Transactional(rollbackFor = Exception.class)
    public void approveStep(String taskId, boolean approved, String comment) {
        if (processEngine == null) {
            throw new BizException(ErrorCode.SYSTEM_ERROR, "Flowable未集成，无法执行审批 / Flowable not integrated, unable to execute approval");
        }

        TaskService taskService = processEngine.getTaskService();
        RuntimeService runtimeService = processEngine.getRuntimeService();

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "审批任务不存在或已完成 / Approval task not found or already completed");
        }

        String processInstanceId = task.getProcessInstanceId();

        // 获取申请ID / Get application ID
        Map<String, Object> processVariables = runtimeService.getVariables(processInstanceId);
        Long applyId = Long.valueOf(processVariables.get("applyId").toString());
        PortalRegister application = portalRegisterMapper.selectById(applyId);
        if (application == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "入驻申请不存在 / Onboarding application not found");
        }

        // 获取当前审批节点名称 / Get current approval node name
        String nodeName = task.getTaskDefinitionKey();

        // 保存审批记录 / Save approval record
        OnboardingApproval record = new OnboardingApproval();
        record.setRegisterId(applyId);
        record.setTaskId(taskId);
        record.setApprovalNode(nodeName);
        record.setApprovalResult(approved ? 1 : 2);
        record.setComment(comment);
        record.setApproverName("系统审批人"); // 实际应取当前登录用户 / Should fetch from current user context
        record.setApprovedAt(LocalDateTime.now());
        approvalMapper.insert(record);

        // 组装审批变量 / Assemble approval variables
        Map<String, Object> variables = new HashMap<>();
        variables.put(nodeName + "_approved", approved);
        variables.put(nodeName + "_comment", comment);
        variables.put(nodeName + "_result", approved ? "通过" : "驳回");

        // 完成当前任务 / Complete current task
        taskService.complete(taskId, variables);

        if (approved) {
            // 判断流程是否已结束（终审通过） / Check if process has ended (final approval passed)
            boolean isProcessEnded = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId).count() == 0;

            if (isProcessEnded) {
                // 审批全部通过 → 创建供应商主数据 / All approvals passed → create supplier master data
                onApprovalPassed(application);
            } else {
                // 仍有下一节点 → 发送通知 / Still has next node → send notification
                Task nextTask = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
                if (nextTask != null) {
                    sendApprovalNotification(application, nextTask.getTaskDefinitionKey());
                }
            }
        } else {
            // 驳回 → 更新申请状态 / Rejected → update application status
            onApprovalRejected(application, comment);
        }

        log.info("审批步骤完成: applyId={}, taskId={}, approved={}, node={}",
                applyId, taskId, approved, nodeName);
    }

    // ==================== 查询审批进度 / Query Approval Progress ====================

    /**
     * 查询入驻申请审批进度 /
     * Query onboarding application approval progress
     *
     * @param applyId 入驻申请ID / Application ID
     * @return 审批进度响应 / Approval progress response
     */
    public RegisterStatusResponse getApprovalProgress(Long applyId) {
        PortalRegister application = portalRegisterMapper.selectById(applyId);
        if (application == null) {
            throw new BizException(ErrorCode.BAD_REQUEST, "入驻申请不存在 / Onboarding application not found");
        }

        // 查询审批记录 / Query approval records
        List<OnboardingApproval> records = approvalMapper.selectList(
                new LambdaQueryWrapper<OnboardingApproval>()
                        .eq(OnboardingApproval::getRegisterId, applyId)
                        .orderByAsc(OnboardingApproval::getCreatedAt)
        );

        List<RegisterStatusResponse.ApprovalNodeInfo> history = records.stream()
                .map(r -> RegisterStatusResponse.ApprovalNodeInfo.builder()
                        .nodeName(r.getApprovalNode())
                        .result(r.getApprovalResult())
                        .comment(r.getComment())
                        .approverName(r.getApproverName())
                        .approvedAt(r.getApprovedAt())
                        .build())
                .collect(Collectors.toList());

        // 计算当前审批节点 / Calculate current approval node
        String currentNode = getCurrentNode(application, records);

        return RegisterStatusResponse.builder()
                .applyId(application.getId())
                .companyName(application.getCompanyName())
                .applyStatus(application.getApplyStatus())
                .statusDesc(getStatusDesc(application.getApplyStatus()))
                .currentNode(currentNode)
                .processInstanceId(application.getProcessInstanceId())
                .approvalHistory(history)
                .rejectReason(application.getRejectReason())
                .supplierId(application.getSupplierId())
                .createdAt(application.getCreatedAt())
                .build();
    }

    // ==================== 审批结果处理 / Approval Result Handlers ====================

    /**
     * 审批全部通过 — 自动创建供应商主数据记录，关联 supplier_id，发送通过通知 /
     * All approvals passed — auto-create supplier master data, associate supplier_id, send approval notification
     *
     * @param application 入驻申请 / Onboarding application
     */
    @Transactional(rollbackFor = Exception.class)
    public void onApprovalPassed(PortalRegister application) {
        // 创建供应商主数据 / Create supplier master data
        Supplier supplier = new Supplier();
        supplier.setSupplierName(application.getCompanyName());
        supplier.setContactPerson(application.getContactName());
        supplier.setContactPhone(application.getContactPhone());
        supplier.setEmail(application.getContactEmail());
        supplier.setSupplierType(1); // 默认生产商 / Default: manufacturer
        supplier.setStatus(1); // 已准入 / Onboarded
        supplier.setGrade(3); // C级起步 / Start from C grade

        // TODO: 根据实际 Supplier 实体字段映射 / Map according to actual Supplier entity fields
        supplierMapper.insert(supplier);

        // 回写 supplier_id / Write back supplier_id
        application.setSupplierId(supplier.getId());
        application.setApplyStatus(2); // 已通过 / Approved
        portalRegisterMapper.updateById(application);

        // 发送通过通知 / Send approval notification
        sendNotification(application.getContactPhone(),
                "恭喜！您的入驻申请已通过审核，请登录系统完善信息 / Congratulations! Your onboarding application has been approved. Please log in to complete your profile.");

        log.info("入驻审批通过，供应商主数据已创建: applyId={}, supplierId={}",
                application.getId(), supplier.getId());
    }

    /**
     * 审批驳回 / Approval rejected
     *
     * @param application 入驻申请 / Onboarding application
     * @param reason      驳回原因 / Reject reason
     */
    @Transactional(rollbackFor = Exception.class)
    public void onApprovalRejected(PortalRegister application, String reason) {
        application.setApplyStatus(3); // 已驳回 / Rejected
        application.setRejectReason(reason);
        portalRegisterMapper.updateById(application);

        // 发送驳回通知 / Send rejection notification
        String message = String.format(
                "您的入驻申请未通过审核，原因：%s / Your onboarding application was not approved. Reason: %s",
                reason != null ? reason : "未提供原因 / No reason provided"
        );
        sendNotification(application.getContactPhone(), message);

        log.info("入驻申请已驳回: applyId={}, reason={}", application.getId(), reason);
    }

    // ==================== 通知辅助方法 / Notification Helpers ====================

    /**
     * 发送审批通知 — 通知审批人有新申请待审批 /
     * Send approval notification — notify approver of new application pending review
     *
     * @param application 入驻申请 / Onboarding application
     * @param node        审批节点 / Approval node
     */
    private void sendApprovalNotification(PortalRegister application, String node) {
        String roleDesc = getApproverRoleDesc(node);
        String message = String.format(
                "有新供应商入驻申请待审批：%s（审批节点：%s） / New supplier onboarding application pending: %s (Approval node: %s)",
                application.getCompanyName(), roleDesc
        );
        log.info("审批通知: role={}, message={}", node, message);
        // TODO: 对接消息中心 / Integrate with message center
    }

    /**
     * 发送短信/邮件通知给供应商 / Send SMS/email notification to supplier
     *
     * @param phone   手机号 / Phone number
     * @param message 消息内容 / Message content
     */
    private void sendNotification(String phone, String message) {
        log.info("通知发送: phone={}, message={}", phone, message);
        // TODO: 对接短信/邮件网关 / Integrate with SMS/email gateway
    }

    /**
     * 采购员代注册时 — 发送短信/邮件给供应商联系人 /
     * When purchaser proxy registers — send SMS/email to supplier contact
     *
     * @param application 入驻申请 / Onboarding application
     */
    public void sendPurchaserProxyNotification(PortalRegister application) {
        String message = String.format(
                "%s已为您在SRM系统中注册账号，请登录完善信息 / %s has registered an account for you in the SRM system. Please log in to complete your information.",
                application.getInitiatorName() != null ? application.getInitiatorName() : "采购员"
        );
        sendNotification(application.getContactPhone(), message);
    }

    // ==================== 内部辅助 / Internal Helpers ====================

    /**
     * 获取当前审批节点 / Get current approval node
     */
    private String getCurrentNode(PortalRegister application, List<OnboardingApproval> records) {
        if (application.getApplyStatus() == 2) return "已完成 / Completed";
        if (application.getApplyStatus() == 3) return "已驳回 / Rejected";
        if (application.getApplyStatus() == 0) return "待审批 / Pending";

        // 按审批节点顺序判定当前到达哪一步 / Determine current step by node order
        List<String> nodeOrder = Arrays.asList("INITIAL_REVIEW", "FIELD_INSPECT", "FINAL_REVIEW");
        for (String node : nodeOrder) {
            boolean hasRecord = records.stream()
                    .anyMatch(r -> node.equals(r.getApprovalNode()));
            if (!hasRecord) {
                return getApproverRoleDesc(node);
            }
        }
        return "终审中 / In Final Review";
    }

    /**
     * 获取审批节点角色描述 / Get approver role description for node
     */
    private String getApproverRoleDesc(String node) {
        return switch (node) {
            case "INITIAL_REVIEW" -> "资质初审（采购主管） / Initial Review (Purchase Supervisor)";
            case "FIELD_INSPECT" -> "现场考察（品控） / Site Inspection (QC)";
            case "FINAL_REVIEW" -> "终审（采购总监） / Final Review (Purchase Director)";
            default -> node;
        };
    }

    /**
     * 获取状态描述 / Get status description
     */
    private String getStatusDesc(Integer status) {
        return switch (status) {
            case 0 -> "待审批 / Pending";
            case 1 -> "审批中 / In Review";
            case 2 -> "已通过 / Approved";
            case 3 -> "已驳回 / Rejected";
            case 4 -> "已撤回 / Withdrawn";
            default -> "未知 / Unknown";
        };
    }
}
