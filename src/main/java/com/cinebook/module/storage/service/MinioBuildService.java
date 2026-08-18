package com.cinebook.module.storage.service;

import com.cinebook.module.storage.dto.PresignUrlRequest;
import com.cinebook.module.storage.type.UploadType;
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
    public String buildObjectKey(UploadType type, String originalFilename, UUID userId) {
        String extension = extractExtension(originalFilename);

        return switch (type) {
            case AVATAR -> {
                if (userId == null) throw new IllegalArgumentException("UserId is required for AVATAR");
                yield "avatars/%s/avatar%s".formatted(userId.toString(), extension);
            }
            case MOVIE_POSTER -> "uploads/posters/%s%s".formatted(UUID.randomUUID(), extension);

            case MOVIE_BACKDROP -> "uploads/backdrops/%s%s".formatted(UUID.randomUUID(), extension);

            default -> throw new IllegalArgumentException("Upload type is not supported!");
        };
    }

    public String buildPublicUrl(String objectKey) {
        return publicBaseUrl + "/" + objectKey;
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) return "";
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}
