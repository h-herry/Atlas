package com.atlas.purchase.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.model.PurchaseCreateRequest.OrderItemRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单数量校验器 — MOQ/EOQ/订货倍数校验 / Order quantity validator — MOQ/EOQ/Order multiple validation
 * <p>
 * 校验规则： / Validation rules:
 * <ol>
 *   <li>数量必须 ≥ 最小起订量（MOQ） / Quantity must be &ge; MOQ</li>
 *   <li>数量必须是订货量倍数的整数倍 / Quantity must be integer multiple of order_qty_multiple</li>
 *   <li>数量低于经济订货量（EOQ）时给出提示 / Warning when quantity is below EOQ</li>
 * </ol>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Component
public class OrderQuantityValidator {

    /**
     * 校验订单明细的 MOQ/EOQ/订货倍数 / Validate MOQ/EOQ/order multiple for each line item
     *
     * @param items 订单明细列表 / Order item list
     * @throws BizException 校验不通过时抛出 / Thrown on validation failure
     */
    public void validate(List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        for (OrderItemRequest item : items) {
            BigDecimal qty = item.getQuantity();
            if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException(6101, "订单数量必须大于 0 / Order quantity must be greater than 0");
            }

            // 最小起订量 MOQ 校验 / MOQ check
            if (item.getMinOrderQty() != null && item.getMinOrderQty().compareTo(BigDecimal.ZERO) > 0) {
                if (qty.compareTo(item.getMinOrderQty()) < 0) {
                    throw new BizException(6102,
                            String.format("物料 [%s] 订单数量 %s 低于最小起订量（MOQ） %s / "
                                    + "Material [%s] quantity %s is below MOQ %s",
                                    item.getSkuName(), qty, item.getMinOrderQty(),
                                    item.getSkuName(), qty, item.getMinOrderQty()));
                }
            }

            // 订货量倍数校验 / Order multiple check
            if (item.getOrderQtyMultiple() != null && item.getOrderQtyMultiple().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal remainder = qty.remainder(item.getOrderQtyMultiple());
                if (remainder.compareTo(BigDecimal.ZERO) != 0) {
                    throw new BizException(6103,
                            String.format("物料 [%s] 订单数量 %s 不是订货量倍数 %s 的整数倍 / "
                                    + "Material [%s] quantity %s is not an integer multiple of %s",
                                    item.getSkuName(), qty, item.getOrderQtyMultiple(),
                                    item.getSkuName(), qty, item.getOrderQtyMultiple()));
                }
            }

            // EOQ 低于建议值提示 / EOQ advisory check (non-blocking)
            if (item.getEconomicOrderQty() != null && item.getEconomicOrderQty().compareTo(BigDecimal.ZERO) > 0) {
                if (qty.compareTo(item.getEconomicOrderQty()) < 0) {
                    log.warn("订单数量 {} 低于经济订货量（EOQ） {}，建议调整——物料: {} / "
                             + "Order qty {} is below EOQ {}, recommend adjustment — material: {}",
                            qty, item.getEconomicOrderQty(), item.getSkuName(),
                            qty, item.getEconomicOrderQty(), item.getSkuName());
                }
            }
        }
    }
}
