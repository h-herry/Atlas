package com.atlas.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码枚举 — 9位编码：模块(2) + 分类(2) + 具体(3) + 预留(2) /
 * Unified error code enum — 9-digit encoding: module(2) + category(2) + detail(3) + reserved(2)
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ==================== 通用 (00) / General ====================
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    TOKEN_EXPIRED(402, "Token已过期，请重新登录"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统繁忙，请稍后重试"),

    // ==================== 用户模块 (01) / User Module ====================
    USER_NOT_EXIST(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    USERNAME_DUPLICATE(1003, "用户名已存在"),
    PASSWORD_ERROR(1004, "密码错误"),
    ROLE_NOT_EXIST(1005, "角色不存在"),
    USER_LOCKED(1007, "账户已被锁定，请15分钟后再试"),
    RATE_LIMIT_EXCEEDED(429, "请求过于频繁，请稍后再试"),
    CIRCUIT_BREAKER_OPEN(12020, "服务熔断，请稍后重试"),
    AUDIT_LOG_WRITE_FAILED(12021, "审计日志写入失败"),

    // ==================== 供应商模块 (02) / Supplier Module ====================
    SUPPLIER_NOT_EXIST(2001, "供应商不存在"),
    SUPPLIER_FROZEN(2002, "供应商已被冻结"),
    QUALIFICATION_EXPIRED(2003, "供应商资质已过期"),

    // ==================== 合同模块 (03) / Contract Module ====================
    CONTRACT_NOT_EXIST(3001, "合同不存在"),
    CONTRACT_APPROVED(3002, "合同已审批，不可重复提交"),
    CONTRACT_AMOUNT_EXCEED(3003, "合同金额超出预算"),

    // ==================== 采购模块 (04) / Purchase Module ====================
    ORDER_NOT_EXIST(4001, "采购订单不存在"),
    ORDER_CANNOT_MODIFY(4002, "当前状态不允许修改订单"),
    ORDER_DUPLICATE(4003, "重复提交（幂等校验未通过）"),

    // ==================== 库存模块 (05) / Inventory Module ====================
    STOCK_INSUFFICIENT(5001, "库存不足"),
    SKU_NOT_EXIST(5002, "SKU不存在"),

    // ==================== 收货模块 (06) / Receipt Module ====================
    RECEIPT_NOT_EXIST(6001, "收货单不存在"),
    RECEIPT_DUPLICATE(6002, "收货单已存在"),
    MQ_SEND_FAILED(6004, "MQ消息发送失败，事务已回滚，请重试"),

    // ==================== 工作流模块 (07) / Workflow Module ====================
    WORKFLOW_NOT_EXIST(7001, "工作流实例不存在"),
    WORKFLOW_TASK_NOT_FOUND(7002, "待审批任务不存在"),

    // ==================== 供应商SRM准入 (08) / Supplier SRM Registration ====================
    RECRUIT_NOTICE_NOT_EXIST(8001, "招募公告不存在"),
    REGISTER_NOT_EXIST(8002, "准入申请不存在"),
    REGISTER_ALREADY_APPROVED(8003, "准入申请已审批，不可重复操作"),
    APPROVAL_NODE_INVALID(8004, "审批节点不合法"),

    // ==================== 供应商SRM评估 (09) / Supplier SRM Evaluation ====================
    EVAL_TEMPLATE_NOT_EXIST(9001, "评估模板不存在"),
    EVALUATION_NOT_EXIST(9002, "绩效考核不存在"),
    EVALUATION_ALREADY_CONFIRMED(9003, "评估已确认，不可修改"),

    // ==================== 供应商SRM协同 (10) / Supplier SRM Collaboration ====================
    FORECAST_NOT_EXIST(10001, "预测计划不存在"),
    DELIVERY_ORDER_NOT_EXIST(10002, "发货单不存在"),
    RECONCILIATION_NOT_EXIST(10003, "对账单不存在"),
    RECONCILIATION_ALREADY_CONFIRMED(10004, "对账单已确认"),

    // ==================== 供应商SRM风险 (11) / Supplier SRM Risk ====================
    RISK_EVENT_NOT_EXIST(11001, "风险事件不存在"),
    ALERT_RULE_NOT_EXIST(11002, "预警规则不存在"),
    SUPPLIER_BLACKLISTED(11003, "供应商已被列入黑名单"),
    BLACKLIST_ALREADY_EXISTS(11004, "供应商已在黑名单中"),

    // ==================== 物料管理 (12) / Material Management ====================
    MATERIAL_NOT_EXIST(12001, "物料不存在"),
    MATERIAL_CODE_DUPLICATE(12002, "物料编码已存在"),
    MATERIAL_DISABLED(12003, "物料已禁用"),
    CATEGORY_NOT_EXIST(12004, "物料分类不存在"),
    CATEGORY_CODE_DUPLICATE(12005, "分类编码已存在"),
    BOM_NOT_EXIST(12006, "BOM不存在"),
    BOM_ALREADY_PUBLISHED(12007, "BOM已发布，不可编辑"),
    BOM_VERSION_DUPLICATE(12008, "该版本BOM已存在"),
    MRP_PLAN_NOT_EXIST(12009, "MRP计划不存在"),
    MRP_ALREADY_CONFIRMED(12010, "MRP计划已确认，不可修改"),
    DELIVERY_NOT_EXIST(12011, "发货单不存在"),
    DELIVERY_ALREADY_RECEIVED(12012, "发货单已收货"),
    PROGRESS_NOT_EXIST(12013, "生产进度记录不存在"),
    INSPECTION_NOT_EXIST(12014, "检验单不存在"),
    INSPECTION_ALREADY_DONE(12015, "检验已完成，不可重复操作"),
    TRACE_NOT_EXIST(12016, "追溯记录不存在"),
    PERFORMANCE_NOT_EXIST(12017, "绩效记录不存在"),
    ANALYSIS_NOT_EXIST(12018, "分析报表不存在"),

    // ==================== 开放平台 (13) / Open Platform ====================
    API_CLIENT_NOT_EXIST(13001, "API客户端不存在"),
    API_CLIENT_DISABLED(13002, "API客户端已被禁用"),
    API_CLIENT_EXPIRED(13007, "API客户端已过期"),
    API_SIGNATURE_INVALID(13003, "API签名验证失败"),
    API_RATE_LIMITED(13004, "API调用频率超限"),
    WEBHOOK_NOT_EXIST(13005, "Webhook订阅不存在"),
    WEBHOOK_CALLBACK_FAILED(13006, "Webhook回调失败"),

    // ==================== 竞价大厅 (04扩展) / Bidding Hall (04 extended) ====================
    HALL_NOT_EXIST(4004, "竞价大厅不存在"),
    HALL_NOT_ACTIVE(4005, "竞价大厅未开启"),
    HALL_ALREADY_ENDED(4006, "竞价大厅已结束"),
    BID_REJECTED_LOW(4007, "报价低于当前最低价，请重新报价"),

    // ==================== 价格库 (04扩展) / Price Library (04 extended) ====================
    PRICE_NOT_EXIST(4008, "价格记录不存在"),
    PRICE_EXPIRED(4009, "价格已过期"),
    PRICE_TREND_NOT_EXIST(4010, "价格走势不存在"),

    // ==================== 合同风险 (03扩展) / Contract Risk (03 extended) ====================
    RISK_CLAUSE_NOT_EXIST(3004, "风险条款不存在"),
    CONTRACT_NOT_SIGNED(3005, "合同尚未签署"),

    // ==================== 供应商配额/整改 (02扩展) / Supplier Quota/Rectification (02 extended) ====================
    QUOTA_NOT_EXIST(2004, "配额记录不存在"),
    RECTIFICATION_NOT_EXIST(2005, "整改单不存在"),
    RECTIFICATION_ALREADY_DONE(2006, "整改已完成或已逾期"),

    // ==================== 结算协同 (10扩展) / Settlement Collaboration (10 extended) ====================
    SETTLEMENT_NOT_EXIST(10005, "结算单不存在"),
    SETTLEMENT_ALREADY_CONFIRMED(10006, "结算单已确认，不可修改"),
    THREE_WAY_MISMATCH(10007, "三单匹配失败：金额不一致"),

    // ==================== 供应商推荐 (04扩展) / Supplier Recommendation (04 extended) ====================
    RECOMMENDATION_NOT_EXIST(4011, "推荐记录不存在"),

    // ==================== 供应商门户 (14) / Supplier Portal ====================
    PORTAL_CERTIFICATE_NOT_EXIST(14001, "资质文件不存在 / Certificate not found"),
    PORTAL_CERTIFICATE_DUPLICATE(14002, "该类型资质已存在有效记录 / Active certificate of this type already exists"),
    PORTAL_CERTIFICATE_EXPIRED(14003, "资质已过期 / Certificate has expired"),
    PORTAL_PROFILE_NOT_EXIST(14004, "供应商档案不存在 / Supplier profile not found"),

    // ==================== 订单模块 (15) / Order Module ====================
    JIT_SCHEDULE_NOT_EXIST(15001, "JIT排程不存在 / JIT schedule not found"),
    JIT_OUTSIDE_WINDOW(15002, "当前不在JIT确认窗口内 / Outside JIT confirmation window"),
    JIT_ALREADY_CONFIRMED(15003, "JIT排程已确认，不可重复操作 / JIT schedule already confirmed"),
    PLANT_NO_SUPPLIER(15004, "该工厂无可供应供应商 / No available supplier for this plant"),
    ALLOCATION_CAPACITY_EXCEEDED(15005, "供应商产能已满，无法继续分单 / Supplier capacity exceeded"),

    // ==================== 交货模块 (16) / Delivery Module ====================
    VMI_INVENTORY_NOT_EXIST(16001, "VMI库存记录不存在 / VMI inventory not found"),
    VMI_STOCK_BELOW_SAFETY(16002, "VMI库存低于安全库存 / VMI stock below safety"),
    VMI_STOCK_ABOVE_MAX(16003, "VMI库存超过最大库存上限 / VMI stock above max"),

    // ==================== 质量模块 (17) / Quality Module ====================
    PPAP_SUBMISSION_NOT_EXIST(17001, "PPAP提交记录不存在 / PPAP submission not found"),
    PPAP_ELEMENT_NOT_EXIST(17002, "PPAP要素不存在 / PPAP element not found"),
    PPAP_LEVEL_INVALID(17003, "PPAP等级必须为1~5 / PPAP level must be 1~5"),
    PPAP_ALREADY_APPROVED(17004, "PPAP已批准，不可修改 / PPAP already approved"),
    LOT_NOT_EXIST(17005, "批次记录不存在 / Lot trace not found"),
    LOT_DUPLICATE(17006, "批次号重复 / Duplicate lot number"),

    // ==================== 物料模块 (18) / Material Module ====================
    LOT_NOT_MANAGED(18001, "物料未启用批次管理 / Material lot management not enabled"),

    // ==================== 通用异常 (00扩展) / General Extended ====================
    ILLEGAL_STATE(400, "操作状态不合法"),
    PARAM_INVALID(400, "参数不合法"),
    DATA_NOT_FOUND(404, "数据不存在"),
    DATA_NOT_EXIST(404, "数据不存在 / Data not exist"),
    OPERATION_FAILED(500, "操作失败"),
    AUCTION_CLOSED(4012, "竞价已关闭"),
    AUCTION_NOT_ACTIVE(4013, "竞价未开启");

    private final int code;
    private final String message;
}
