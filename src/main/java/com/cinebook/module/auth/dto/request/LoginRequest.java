package com.cinebook.module.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest (
        @NotBlank(message = "Email must not be empty")
        @Email(message = "Email is not in right format")
        String email,

        @NotBlank(message = "Password must not be empty")
        String password,

        @NotBlank
        String deviceId
) {}
