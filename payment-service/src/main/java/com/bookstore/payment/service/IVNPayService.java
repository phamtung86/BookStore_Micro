package com.bookstore.payment.service;

import com.bookstore.payment.dto.VNPayRequest;
import com.bookstore.payment.dto.VNPayResponse;

import java.util.Map;

public interface IVNPayService {

    String createPaymentUrl(VNPayRequest request);

    VNPayResponse processCallback(Map<String, String> params);

    VNPayResponse queryPaymentStatus(String txnRef);
}
