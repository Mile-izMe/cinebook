package com.cinebook.module.payment.gateway;

import com.cinebook.module.payment.dto.response.CallbackPayload;
import com.cinebook.module.payment.dto.response.PaymentResult;

import java.util.UUID;

public interface PaymentGateway {

    PaymentResult createPayment(UUID paymentId, int amount, String bookingCode);

    boolean verifySignature(CallbackPayload payload);

}
