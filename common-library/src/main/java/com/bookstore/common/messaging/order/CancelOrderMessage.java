package com.bookstore.common.messaging.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelOrderMessage {

    private Long orderId;
    private Long userId;
    private String reason;
}
