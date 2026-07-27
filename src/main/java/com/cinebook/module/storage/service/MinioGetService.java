package com.cinebook.module.storage.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioGetService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    /**
     * Confirms the client actually uploaded the object before we trust the
     * objectKey they send back in POST /movies - never take their word for it.
     */
    public boolean objectExists(String objectKey) {
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucket).object(objectKey).build());
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) return false;
            throw new CinebookException(ErrorCode.FILE_UPLOAD_FAILED);
        } catch (Exception e) {
            throw new CinebookException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public void requireObjectExists(String objectKey) {
        if (!objectExists(objectKey)) {
            throw new CinebookException(ErrorCode.VALIDATION_ERROR, "Poster not uploaded yet or objectKey not valid!");
        }
    }

    /**
     * Only needed if the bucket is private - if public, MinioBuildService.buildPublicUrl() is enough.
     */
    public String generatePresignedGetUrl(String objectKey, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Http.Method.GET)
                    .bucket(bucket)
                    .object(objectKey)
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new CinebookException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }
}
