package com.cinebook.module.payment.dto.response;

public record CallbackPayload(
        String providerTransactionId,
        int amount,
        String status,
        long timestamp,
        String signature
) {
}
