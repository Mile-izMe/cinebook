package com.cinebook.module.storage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Pure naming/URL-building logic - no I/O, no MinioClient dependency.
 * Kept separate from Read/Write services so the object-key convention can be
 * unit-tested without a running MinIO instance.
 */
@Service
public class MinioBuildService {

    @Value("${minio.public-base-url}")
    private String publicBaseUrl;

    /**
     * Temp upload path - not yet tied to any movieId (movie doesn't exist yet at presign time).
     */
    public String buildImageObjectKey(String imageType, String originalFileName) {
        String extension = extractExtension(originalFileName);
        return "uploads/%s/%s%s".formatted(imageType, UUID.randomUUID(), extension);
        // imageType="poster" -> uploads/posters/uuid.jpg
        // imageType="backdrop" -> uploads/backdrops/uuid.jpg
    }

    public String buildPublicUrl(String objectKey) {
        return publicBaseUrl + "/" + objectKey;
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) return "";
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}
