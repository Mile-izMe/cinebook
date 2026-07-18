package com.cinebook.module.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

    @NotBlank(message = "username must not be empty")
    @Size(max = 100)
    String userName,

    @NotBlank(message = "Email must not be empty")
    @Email(message = "Email is not in right format")
    String email,

    @NotBlank(message = "Phone must not be empty")
    @Pattern(regexp = "^[0-9]{9,15}$", message = "Phone number is not valid")
    String phone,

    @NotBlank(message = "Password must not be empty")
    @Size(min = 8, message = "Password must have at lease 8 characters")
    String password

) {}
