package com.bookstore.payment.service;

import com.bookstore.common.dto.response.ServiceResponse;
import com.bookstore.payment.dto.PaymentInput;

public interface IPaymentService {

    ServiceResponse createPayment(PaymentInput paymentInput);
}
