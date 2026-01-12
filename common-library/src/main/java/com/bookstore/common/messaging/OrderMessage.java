package com.bookstore.common.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage implements Serializable {

    private Long id;
    private String orderNumber;
    private Long userId;
    private String status;
    private String currency;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
}
