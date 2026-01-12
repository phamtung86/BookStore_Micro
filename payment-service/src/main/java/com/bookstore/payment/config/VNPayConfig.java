package com.bookstore.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "vnpay")
public class VNPayConfig {

    private String tmnCode = "QECDEV55";
    private String hashSecret = "EY8V7A5T9D43OLFYEJ8X126S4AGACNEK";
    private String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private String returnUrl = "http://localhost:8080/api/v1/payments/vnpay/callback";
    private String apiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";
    private String version = "2.1.0";
    private String command = "pay";
    private String orderType = "other";
}
