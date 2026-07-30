package com.cinebook.module.cinema.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record CinemaCreateRequest(
        @NotNull(message = "City must not empty!")
        UUID cityId,

        @NotBlank(message = "Cinema name must not be empty!")
        String name,

        @NotBlank(message = "Address must not be empty!")
        String address,

        BigDecimal latitude,
        BigDecimal longitude
) {
}
