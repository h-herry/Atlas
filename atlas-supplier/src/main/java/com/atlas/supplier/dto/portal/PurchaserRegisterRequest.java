package com.atlas.supplier.dto.portal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 采购员代供应商注册请求 DTO — 继承 RegisterRequest，额外增加采购员信息 /
 * Purchaser proxy registration request DTO — extends RegisterRequest, adds purchaser info
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PurchaserRegisterRequest extends RegisterRequest {

    /** 采购员ID / Purchaser ID */
    @NotNull(message = "采购员ID不能为空 / Purchaser ID is required")
    private Long initiatorId;

    /** 采购员姓名 / Purchaser name */
    @NotBlank(message = "采购员姓名不能为空 / Purchaser name is required")
    private String initiatorName;
}
