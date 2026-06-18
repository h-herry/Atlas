package com.atlas.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 库存扣减响应 DTO / Inventory deduction response DTO
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeductResponse {

    /** 是否成功 / Success flag */
    private Boolean success;

    /** 扣减后库存 / Stock after deduction */
    private BigDecimal newStockQty;

    /** 扣减后版本号 / Version after deduction */
    private Integer newVersion;

    /** 提示信息 / Message */
    private String message;
}
