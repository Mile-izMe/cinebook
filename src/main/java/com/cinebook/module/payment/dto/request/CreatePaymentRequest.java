package com.cinebook.module.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePaymentRequest(

        @NotBlank(message = "Payment method must not be empty!")
        String paymentMethod

) {
}
