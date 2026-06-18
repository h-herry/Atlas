package com.atlas.inventory.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存扣减请求 DTO / Inventory deduction request DTO
 * <p>
 * 包含乐观锁 version 字段用于并发控制。 / Includes optimistic lock version field for concurrency control.
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Data
public class DeductRequest {

    /** SKU ID */
    @NotNull(message = "SKU ID 不能为空")
    private Long skuId;

    /** 仓库ID / Warehouse ID */
    @NotNull(message = "仓库ID 不能为空")
    private Long warehouseId;

    /** 扣减数量 / Deduction quantity */
    @NotNull(message = "扣减数量不能为空")
    @Positive(message = "扣减数量必须大于0")
    private BigDecimal qty;

    /** 当前版本号（乐观锁） / Current version (optimistic lock) */
    @NotNull(message = "版本号不能为空")
    private Integer version;

    /** 关联订单号 / Related order number */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /** 操作人ID / Operator ID */
    private Long operatorId;
}
