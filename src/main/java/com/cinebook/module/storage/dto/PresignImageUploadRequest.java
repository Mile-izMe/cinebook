package com.cinebook.module.storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PresignImageUploadRequest(
        @NotBlank(message = "File name must not be empty!")
        String fileName,

        @NotBlank(message = "Content-Type must not be empty!")
        String contentType,

        @NotBlank(message = "imageType must not be empty!")
        @Pattern(regexp = "^(poster|backdrop)$", message = "imageType accepts 'poster' or 'backdrop'")
        String imageType
) {
}