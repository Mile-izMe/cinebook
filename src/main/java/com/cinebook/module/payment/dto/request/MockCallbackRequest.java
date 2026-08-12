package com.cinebook.module.payment.dto.request;

public record MockCallbackRequest(
        String providerTransactionId,
        int amount,
        String status,   // "SUCCESS" or "FAILED"
        long timestamp,
        String signature
) {
}
