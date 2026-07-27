package com.cinebook.module.review.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(

        @NotNull(message = "Rating must not be empty!")
        @DecimalMin(value = "1.0", message = "Lowest rating must be 1!")
        @DecimalMax(value = "5.0", message = "Highest rating must be 5!")
        java.math.BigDecimal rating,

        String comment
) {
}