package com.bookstore.payment.service.Impl;

import com.bookstore.common.exception.BusinessException;
import com.bookstore.common.messaging.RabbitMQConstants;
import com.bookstore.common.messaging.order.CancelOrderMessage;
import com.bookstore.payment.client.OrderClient;
import com.bookstore.payment.config.VNPayConfig;
import com.bookstore.payment.dto.VNPayRequest;
import com.bookstore.payment.dto.VNPayResponse;
import com.bookstore.payment.entity.Payment;
import com.bookstore.payment.repository.PaymentRepository;
import com.bookstore.payment.service.IVNPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements IVNPayService {

    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;

    private static final DateTimeFormatter VNPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public String createPaymentUrl(VNPayRequest request) {


        String txnRef = generateTxnRef(request.getOrderId());
        long amountInVND = request.getAmount().multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", vnPayConfig.getVersion());
        params.put("vnp_Command", vnPayConfig.getCommand());
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amountInVND));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", request.getOrderInfo() != null ? request.getOrderInfo()
                : "Payment for order " + request.getOrderNumber());
        params.put("vnp_OrderType", vnPayConfig.getOrderType());
        params.put("vnp_Locale", request.getLocale() != null ? request.getLocale() : "vn");
        params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        params.put("vnp_IpAddr", request.getIpAddress() != null ? request.getIpAddress() : "127.0.0.1");
        params.put("vnp_CreateDate", LocalDateTime.now().format(VNPAY_DATE_FORMAT));
        params.put("vnp_ExpireDate", LocalDateTime.now().plusMinutes(15).format(VNPAY_DATE_FORMAT));

        if (request.getBankCode() != null && !request.getBankCode().isEmpty()) {
            params.put("vnp_BankCode", request.getBankCode());
        }

        StringBuilder query = new StringBuilder();
        StringBuilder hashData = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!query.isEmpty()) {
                query.append("&");
                hashData.append("&");
            }
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            hashData.append(entry.getKey())
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        String secureHash = hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return vnPayConfig.getPayUrl() + "?" + query;
    }

    @Override
    @Transactional
    public VNPayResponse processCallback(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        String signValue = buildHashData(params);
        String calculatedHash = hmacSHA512(vnPayConfig.getHashSecret(), signValue);

        if (!calculatedHash.equals(vnpSecureHash)) {
            return VNPayResponse.failed("97", "Invalid Checksum");
        }

        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        String transactionNo = params.get("vnp_TransactionNo");
        String amountStr = params.get("vnp_Amount");
        BigDecimal amount = new BigDecimal(amountStr).divide(BigDecimal.valueOf(100));

        Long orderId = extractOrderId(txnRef);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("Payment not found for order: " + orderId));

        VNPayResponse response;

        if ("00".equals(responseCode)) {
            payment.markAsCompleted(transactionNo, responseCode, "Success");
            payment.setBankCode(params.get("vnp_BankCode"));
            payment.setBankTransactionNo(params.get("vnp_BankTranNo"));

            response = VNPayResponse.builder()
                    .success(true)
                    .message("Payment successful")
                    .responseCode(responseCode)
                    .transactionNo(transactionNo)
                    .txnRef(txnRef)
                    .amount(amount)
                    .bankCode(params.get("vnp_BankCode"))
                    .bankTranNo(params.get("vnp_BankTranNo"))
                    .cardType(params.get("vnp_CardType"))
                    .payDate(params.get("vnp_PayDate"))
                    .orderId(orderId)
                    .build();

        } else {
            String errorMessage = getErrorMessage(responseCode);
            payment.markAsFailed(responseCode, errorMessage);

            response = VNPayResponse.failed(responseCode, errorMessage);
            response.setOrderId(orderId);
            response.setTxnRef(txnRef);
            CancelOrderMessage cancelOrderMessage = CancelOrderMessage.builder()
                    .orderId(orderId)
                    .userId(payment.getUserId())
                    .reason("TRANSACTION ERROR")
                    .build();
            orderClient.cancelOrder(cancelOrderMessage);
        }
        paymentRepository.save(payment);
        return response;
    }

    @Override
    public VNPayResponse queryPaymentStatus(String txnRef) {
        return VNPayResponse.builder()
                .txnRef(txnRef)
                .message("Query not implemented yet")
                .build();
    }

    private String generateTxnRef(Long orderId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "BO_"+orderId + "_" + timestamp;
    }

    private Long extractOrderId(String txnRef) {
        try {
            return Long.parseLong(txnRef.split("_")[1]);
        } catch (Exception e) {
            throw new BusinessException("Invalid txnRef format: " + txnRef);
        }
    }

    private String buildHashData(Map<String, String> params) {
        TreeMap<String, String> sortedParams = new TreeMap<>(params);
        StringBuilder hashData = new StringBuilder();

        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                if (!hashData.isEmpty()) {
                    hashData.append("&");
                }
                hashData.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }
        }
        return hashData.toString();
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKeySpec);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA512", e);
        }
    }

    private String getErrorMessage(String responseCode) {
        return switch (responseCode) {
            case "07" -> "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)";
            case "09" -> "Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking";
            case "10" -> "Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11" -> "Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch";
            case "12" -> "Thẻ/Tài khoản của khách hàng bị khóa";
            case "13" -> "Quý khách nhập sai mật khẩu xác thực giao dịch (OTP)";
            case "24" -> "Khách hàng hủy giao dịch";
            case "51" -> "Tài khoản của quý khách không đủ số dư để thực hiện giao dịch";
            case "65" -> "Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày";
            case "75" -> "Ngân hàng thanh toán đang bảo trì";
            case "79" -> "KH nhập sai mật khẩu thanh toán quá số lần quy định";
            case "99" -> "Lỗi không xác định";
            default -> "Giao dịch thất bại. Mã lỗi: " + responseCode;
        };
    }
}
