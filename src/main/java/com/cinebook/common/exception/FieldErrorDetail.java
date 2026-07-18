package com.cinebook.common.exception;

public record FieldErrorDetail(
        String field,
        String message
) {}