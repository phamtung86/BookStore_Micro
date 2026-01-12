package com.bookstore.order.dto.order;

import com.bookstore.order.dto.orderItem.OrderItemRequest;
import com.bookstore.order.entity.Order;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItemRequest> items;

    private String couponCode;

    @NotBlank(message = "Recipient name is required")
    private String shippingRecipientName;

    @NotBlank(message = "Recipient phone is required")
    private String shippingPhone;

    @NotBlank(message = "Province is required")
    private String shippingProvince;

    private String shippingWard;

    @NotBlank(message = "Address is required")
    private String shippingAddress;

    @NotNull(message = "Payment method is required")
    private Order.PaymentMethod paymentMethod;

    private String customerNote;

}
