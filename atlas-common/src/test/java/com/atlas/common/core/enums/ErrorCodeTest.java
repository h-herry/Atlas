package com.atlas.common.core.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErrorCode 枚举测试")
class ErrorCodeTest {

    @Test
    @DisplayName("通用错误码应返回正确 code 和 message")
    void should_return_correct_common_codes() {
        assertThat(ErrorCode.SUCCESS.getCode()).isEqualTo(200);
        assertThat(ErrorCode.SUCCESS.getMessage()).isEqualTo("操作成功");

        assertThat(ErrorCode.BAD_REQUEST.getCode()).isEqualTo(400);
        assertThat(ErrorCode.BAD_REQUEST.getMessage()).isEqualTo("请求参数错误");

        assertThat(ErrorCode.UNAUTHORIZED.getCode()).isEqualTo(401);
        assertThat(ErrorCode.FORBIDDEN.getCode()).isEqualTo(403);
        assertThat(ErrorCode.NOT_FOUND.getCode()).isEqualTo(404);
        assertThat(ErrorCode.INTERNAL_ERROR.getCode()).isEqualTo(500);
    }

    @Test
    @DisplayName("用户模块错误码应返回正确值")
    void should_return_correct_user_codes() {
        assertThat(ErrorCode.USER_NOT_EXIST.getCode()).isEqualTo(1001);
        assertThat(ErrorCode.USER_DISABLED.getCode()).isEqualTo(1002);
        assertThat(ErrorCode.USERNAME_DUPLICATE.getCode()).isEqualTo(1003);
        assertThat(ErrorCode.PASSWORD_ERROR.getCode()).isEqualTo(1004);
        assertThat(ErrorCode.ROLE_NOT_EXIST.getCode()).isEqualTo(1005);
    }

    @Test
    @DisplayName("供应商模块错误码应返回正确值")
    void should_return_correct_supplier_codes() {
        assertThat(ErrorCode.SUPPLIER_NOT_EXIST.getCode()).isEqualTo(2001);
        assertThat(ErrorCode.SUPPLIER_FROZEN.getCode()).isEqualTo(2002);
        assertThat(ErrorCode.QUALIFICATION_EXPIRED.getCode()).isEqualTo(2003);
    }

    @Test
    @DisplayName("合同模块错误码应返回正确值")
    void should_return_correct_contract_codes() {
        assertThat(ErrorCode.CONTRACT_NOT_EXIST.getCode()).isEqualTo(3001);
        assertThat(ErrorCode.CONTRACT_APPROVED.getCode()).isEqualTo(3002);
        assertThat(ErrorCode.CONTRACT_AMOUNT_EXCEED.getCode()).isEqualTo(3003);
    }

    @Test
    @DisplayName("采购模块错误码应返回正确值")
    void should_return_correct_purchase_codes() {
        assertThat(ErrorCode.ORDER_NOT_EXIST.getCode()).isEqualTo(4001);
        assertThat(ErrorCode.ORDER_CANNOT_MODIFY.getCode()).isEqualTo(4002);
        assertThat(ErrorCode.ORDER_DUPLICATE.getCode()).isEqualTo(4003);
    }

    @Test
    @DisplayName("库存模块错误码应返回正确值")
    void should_return_correct_inventory_codes() {
        assertThat(ErrorCode.STOCK_INSUFFICIENT.getCode()).isEqualTo(5001);
        assertThat(ErrorCode.SKU_NOT_EXIST.getCode()).isEqualTo(5002);
    }

    @Test
    @DisplayName("收货模块错误码应返回正确值")
    void should_return_correct_receipt_codes() {
        assertThat(ErrorCode.RECEIPT_NOT_EXIST.getCode()).isEqualTo(6001);
        assertThat(ErrorCode.RECEIPT_DUPLICATE.getCode()).isEqualTo(6002);
    }

    @Test
    @DisplayName("工作流模块错误码应返回正确值")
    void should_return_correct_workflow_codes() {
        assertThat(ErrorCode.WORKFLOW_NOT_EXIST.getCode()).isEqualTo(7001);
        assertThat(ErrorCode.WORKFLOW_TASK_NOT_FOUND.getCode()).isEqualTo(7002);
    }
}
