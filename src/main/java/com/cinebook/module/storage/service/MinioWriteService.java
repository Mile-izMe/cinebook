package com.cinebook.module.storage.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.module.storage.dto.PresignUrlResponse;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MinioWriteService {

    private static final Logger log = LoggerFactory.getLogger(MinioWriteService.class);
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024; // 5MB
    private static final int PRESIGN_TTL_MINUTES = 10;

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @PostConstruct
    public void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created MinIO bucket: {}", bucket);
            }

            String policyJson = """
                    {
                      "Version": "2012-10-17",
                      "Statement": [
                        {
                          "Effect": "Allow",
                          "Principal": "*",
                          "Action": ["s3:GetObject"],
                          "Resource": ["arn:aws:s3:::%s/*"]
                        }
                      ]
                    }
                    """.formatted(bucket);

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucket)
                            .config(policyJson)
                            .build()
            );
            log.info("Verified/Set public read policy for bucket: {}", bucket);
        } catch (Exception e) {
            log.warn("Could not verify/create MinIO bucket '{}': {}", bucket, e.getMessage());
        }
    }

    /**
     * Generates a presigned POST policy - constraints (content-type prefix,
     * size range, exact key) are embedded in the signature itself, so the
     * browser cannot upload something outside these bounds even if it tries.
     */
    public PresignUrlResponse createPresignedImageUpload(String objectKey, String contentType) {
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new CinebookException(ErrorCode.INVALID_FILE_TYPE);
        }

        try {
            PostPolicy policy = new PostPolicy(bucket, ZonedDateTime.now().plusMinutes(PRESIGN_TTL_MINUTES));
            policy.addEqualsCondition("key", objectKey);
            policy.addEqualsCondition("Content-Type", contentType);
            policy.addContentLengthRangeCondition(1, MAX_FILE_SIZE_BYTES);

            var formData = minioClient.getPresignedPostFormData(policy);

            return new PresignUrlResponse(
                    minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                                    .method(Http.Method.PUT).bucket(bucket).object(objectKey).build())
                            .replaceAll("\\?.*$", ""), // base endpoint URL for the POST form action
                    formData,
                    objectKey,
                    PRESIGN_TTL_MINUTES * 60
            );
        } catch (Exception e) {
            log.error("Failed to generate presigned upload for objectKey={}", objectKey, e);
            throw new CinebookException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public void deleteObject(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) return;
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
        } catch (Exception e) {
            // Don't fail the whole request just because cleanup of an old poster failed -
            // log it and move on; a stray object in MinIO is a minor cost, not a correctness issue.
            log.warn("Failed to delete old poster object '{}': {}", objectKey, e.getMessage());
        }
    }
}
