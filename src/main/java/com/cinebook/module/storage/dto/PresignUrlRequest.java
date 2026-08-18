package com.cinebook.module.storage.dto;

import com.cinebook.module.storage.type.UploadType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PresignUrlRequest(
        @NotBlank(message = "File name must not be empty!")
        String fileName,

        @NotBlank(message = "Content-Type must not be empty!")
        String contentType,

        @NotNull(message = "Upload type must not be null!")
        UploadType type
) {
}