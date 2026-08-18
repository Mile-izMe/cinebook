package com.cinebook.module.storage.controller;

import com.cinebook.common.response.ApiSuccessResponse;
import com.cinebook.common.security.CustomerUserDetails;
import com.cinebook.module.storage.dto.PresignUrlRequest;
import com.cinebook.module.storage.dto.PresignUrlResponse;
import com.cinebook.module.storage.service.MinioBuildService;
import com.cinebook.module.storage.service.MinioWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
public class StorageController {

    private final MinioBuildService minioBuildService;
    private final MinioWriteService minioWriteService;

    @PostMapping("/presigned-url")
    public ResponseEntity<ApiSuccessResponse<PresignUrlResponse>> presign(
            @Valid @RequestBody PresignUrlRequest request,
            @AuthenticationPrincipal CustomerUserDetails userDetails
    ) {
        UUID userId = userDetails.getUserId();

        String objectKey = minioBuildService.buildObjectKey(
                request.type(),
                request.fileName(),
                userId
        );

        PresignUrlResponse response = minioWriteService.createPresignedImageUpload(
                objectKey,
                request.contentType()
        );

        return ResponseEntity.ok(ApiSuccessResponse.<PresignUrlResponse>builder()
                .message("Presigned upload URL generated")
                .data(response)
                .build());
    }
}