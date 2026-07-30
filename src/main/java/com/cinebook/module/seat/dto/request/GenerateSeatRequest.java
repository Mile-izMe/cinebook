package com.cinebook.module.seat.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GenerateSeatRequest(
        @NotNull @Min(1) @Max(26) // A-Z
        Integer rows,

        @NotNull @Min(1) @Max(50)
        Integer columns
) {
}
