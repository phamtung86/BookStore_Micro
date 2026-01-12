package com.bookstore.common.messaging.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSagaStartEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sagaId;
    private Long orderId;
    private Long userId;
    private String orderNumber;
    private List<SagaOrderItem> items;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String paymentMethod;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SagaOrderItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long productId;
        private String sku;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}
