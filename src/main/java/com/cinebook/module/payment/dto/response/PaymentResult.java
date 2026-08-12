package com.cinebook.module.payment.dto.response;

public record PaymentResult(
        String paymentUrl,
        String providerTransactionId
) {
}
