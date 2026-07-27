package com.cinebook.module.storage.dto;

import java.util.Map;

public record PresignedUploadResult(
        String uploadUrl,               // POST to this URL (endpoint + bucket)
        Map<String, String> formData,   // Required fields (policy, signature, key...)
        String objectKey,
        int expiresInSeconds
) {
}