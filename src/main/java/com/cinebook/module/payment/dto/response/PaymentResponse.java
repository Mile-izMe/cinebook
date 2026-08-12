package com.cinebook.module.payment.dto.response;

import com.cinebook.module.payment.entity.PaymentStatus;

import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID bookingId,
        Integer amount,
        PaymentStatus status,
        String paymentUrl
) {
}
