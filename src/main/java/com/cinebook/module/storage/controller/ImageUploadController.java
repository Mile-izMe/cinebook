package com.cinebook.module.storage.controller;

import com.cinebook.common.response.ApiSuccessResponse;
import com.cinebook.module.storage.dto.PresignImageUploadRequest;
import com.cinebook.module.storage.dto.PresignedUploadResult;
import com.cinebook.module.storage.service.MinioBuildService;
import com.cinebook.module.storage.service.MinioWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies/image")
@RequiredArgsConstructor
public class ImageUploadController {

    private final MinioBuildService minioBuildService;
    private final MinioWriteService minioWriteService;

    // Admin-only - enforce in SecurityConfig alongside the other movie-write rules
    @PostMapping("/presign")
    public ResponseEntity<ApiSuccessResponse<PresignedUploadResult>> presign(
            @Valid @RequestBody PresignImageUploadRequest request) {

        String objectKey = minioBuildService.buildImageObjectKey(request.imageType(), request.fileName());
        PresignedUploadResult result = minioWriteService.createPresignedImageUpload(objectKey, request.contentType());

        return ResponseEntity.ok(ApiSuccessResponse.<PresignedUploadResult>builder()
                .message("Presigned upload URL generated")
                .data(result)
                .build());
    }
}