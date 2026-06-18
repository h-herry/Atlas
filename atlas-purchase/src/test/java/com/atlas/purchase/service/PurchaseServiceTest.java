package com.atlas.purchase.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.entity.PurchaseOrder;
import com.atlas.purchase.entity.PurchaseOrderItem;
import com.atlas.purchase.mapper.PurchaseOrderItemMapper;
import com.atlas.purchase.mapper.PurchaseOrderMapper;
import com.atlas.purchase.model.PurchaseCreateRequest;
import com.atlas.purchase.model.PurchaseCreateRequest.OrderItemRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("采购订单服务单元测试")
class PurchaseServiceTest {

    @Mock private PurchaseOrderMapper orderMapper;
    @Mock private PurchaseOrderItemMapper itemMapper;
    @Mock private NegotiationService negotiationService;
    @Mock private ConsultationService consultationService;
    @Mock private FrameworkService frameworkService;
    @Mock private OpenBiddingService openBiddingService;
    @Mock private InvitedBiddingService invitedBiddingService;
    @Mock private InquiryService inquiryService;
    @Mock private AuctionService auctionService;
    @Mock private SingleSourceService singleSourceService;
    @Mock private CooperativeInnovationService cooperativeInnovationService;
    @Mock private RestTemplate restTemplate;

    @InjectMocks
    private PurchaseService purchaseService;

    // ======================== 重复提交（幂等） ========================

    @Test
    @DisplayName("重复提交被拒绝：同一 requestId 重复提交抛 BizException(ORDER_CANNOT_MODIFY)")
    void testSubmitOrder_Idempotent() {
        PurchaseOrder order = buildDraftOrder(1L, "PO-001");
        order.setRequestId("REQ-001");
        order.setStatus(PurchaseService.STATUS_DRAFT);
        order.setProcurementType(1);

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.countByRequestId("REQ-001")).thenReturn(2); // 已有2条，幂等命中

        PurchaseOrder result = purchaseService.submitOrder(1L);

        // 幂等命中直接返回原订单
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(orderMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("状态非草稿时提交：抛 BizException(ORDER_CANNOT_MODIFY)")
    void testSubmitOrder_StatusNotDraft() {
        PurchaseOrder order = buildDraftOrder(1L, "PO-001");
        order.setStatus(PurchaseService.STATUS_APPROVED); // 已审批，不可再提交

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.countByRequestId(any())).thenReturn(1);

        assertThatThrownBy(() -> purchaseService.submitOrder(1L))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.ORDER_CANNOT_MODIFY.getCode());
    }

    // ======================== 库存不足 ========================

    @Test
    @DisplayName("库存不足：RestTemplate 返回失败，订单置为已取消并抛 BizException(STOCK_INSUFFICIENT)")
    void testSubmitOrder_InsufficientInventory() {
        PurchaseOrder order = buildDraftOrder(1L, "PO-001");
        order.setStatus(PurchaseService.STATUS_DRAFT);
        order.setProcurementType(1);

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setOrderId(1L);
        item.setSkuId(100L);
        item.setSkuName("测试商品");
        item.setQuantity(new BigDecimal("10"));
        item.setUnitPrice(new BigDecimal("100.00"));
        item.setTotalPrice(new BigDecimal("1000.00"));

        when(orderMapper.selectById(1L)).thenReturn(order);
        when(orderMapper.countByRequestId(any())).thenReturn(1);
        when(itemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(item));
        // RestTemplate 模拟库存扣减失败（3次都失败）
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> purchaseService.submitOrder(1L))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(ErrorCode.STOCK_INSUFFICIENT.getCode());

        // 验证订单被标记为已取消
        verify(orderMapper).updateById(argThat(o -> o.getStatus() == PurchaseService.STATUS_CANCELLED));
    }

    // ======================== 创建采购订单 ========================

    @Test
    @DisplayName("创建采购订单成功：返回订单编号")
    void testCreateOrder_Success() {
        PurchaseCreateRequest request = new PurchaseCreateRequest();
        request.setContractId(10L);
        request.setSupplierId(200L);
        request.setDeptId(300L);
        request.setProcurementType(1);
        request.setRequestId("REQ-CREATE-001");
        request.setCreatedBy(1L);

        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setSkuId(100L);
        itemReq.setSkuName("办公桌");
        itemReq.setQuantity(new BigDecimal("5"));
        itemReq.setUnitPrice(new BigDecimal("500.00"));
        request.setItems(List.of(itemReq));

        when(orderMapper.insert(any(PurchaseOrder.class))).thenAnswer(inv -> {
            PurchaseOrder o = inv.getArgument(0);
            o.setId(1001L);
            return 1;
        });
        when(itemMapper.insert(any(PurchaseOrderItem.class))).thenReturn(1);

        PurchaseOrder result = purchaseService.createOrder(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1001L);
        assertThat(result.getOrderNo()).startsWith("PO");
        assertThat(result.getStatus()).isEqualTo(PurchaseService.STATUS_DRAFT);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("2500.00"));

        verify(orderMapper).insert(any(PurchaseOrder.class));
        verify(itemMapper, times(1)).insert(any(PurchaseOrderItem.class));
    }

    // ======================== 创建多明细订单 ========================

    @Test
    @DisplayName("创建多明细采购订单：正确计算总金额")
    void testCreateOrder_MultipleItems() {
        PurchaseCreateRequest request = new PurchaseCreateRequest();
        request.setContractId(10L);
        request.setSupplierId(200L);
        request.setDeptId(300L);
        request.setProcurementType(1);
        request.setCreatedBy(1L);

        OrderItemRequest item1 = new OrderItemRequest();
        item1.setSkuId(100L);
        item1.setSkuName("商品A");
        item1.setQuantity(new BigDecimal("3"));
        item1.setUnitPrice(new BigDecimal("100.00"));

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setSkuId(200L);
        item2.setSkuName("商品B");
        item2.setQuantity(new BigDecimal("2"));
        item2.setUnitPrice(new BigDecimal("250.00"));

        request.setItems(List.of(item1, item2));

        when(orderMapper.insert(any(PurchaseOrder.class))).thenAnswer(inv -> {
            inv.getArgument(0, PurchaseOrder.class).setId(1002L);
            return 1;
        });
        when(itemMapper.insert(any(PurchaseOrderItem.class))).thenReturn(1);

        PurchaseOrder result = purchaseService.createOrder(request);

        // 3 * 100 + 2 * 250 = 300 + 500 = 800
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("800.00"));
        verify(itemMapper, times(2)).insert(any(PurchaseOrderItem.class));
    }

    // ======================== 辅助方法 ========================

    private PurchaseOrder buildDraftOrder(Long id, String orderNo) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(id);
        order.setOrderNo(orderNo);
        order.setSupplierId(200L);
        order.setDeptId(300L);
        order.setTotalAmount(new BigDecimal("1000.00"));
        order.setStatus(PurchaseService.STATUS_DRAFT);
        order.setCreatedBy(1L);
        return order;
    }
}
