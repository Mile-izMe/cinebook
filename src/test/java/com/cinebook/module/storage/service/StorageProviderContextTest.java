package com.cinebook.module.storage.service;

import com.cinebook.common.config.MinioConfig;
import com.cinebook.module.storage.type.UploadType;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class StorageProviderContextTest {
    private ApplicationContextRunner runner() {
        return new ApplicationContextRunner()
                .withUserConfiguration(CloudinaryStorageService.class, MinioBuildService.class,
                        MinioGetService.class, MinioWriteService.class)
                .withPropertyValues("minio.bucket=cinebook", "minio.public-base-url=http://localhost:9000/cinebook");
    }

    @Test void minioStillWorksWithoutCloudinaryCredentials() {
        runner().withBean(MinioClient.class, () -> mock(MinioClient.class)).run(context -> {
            assertNull(context.getStartupFailure());
            assertEquals(0, context.getBeansOfType(CloudinaryStorageService.class).size());
            var build = context.getBean(MinioBuildService.class);
            UUID user = UUID.randomUUID();
            assertEquals("avatars/" + user + "/avatar.png", build.buildObjectKey(UploadType.AVATAR, "avatar.png", user));
            assertEquals("http://localhost:9000/cinebook/poster.png", build.buildPublicUrl("poster.png"));
            assertEquals("https://image.tmdb.org/poster.png", build.buildPublicUrl("https://image.tmdb.org/poster.png"));
        });
    }

    @Test void cloudinaryStartsWithoutAMinioClientAndKeepsUploadContract() {
        runner().withUserConfiguration(MinioConfig.class).withPropertyValues(
                "app.storage.provider=cloudinary", "cloudinary.cloud-name=demo-cloud",
                "cloudinary.api-key=test-key", "cloudinary.api-secret=test-secret").run(context -> {
            assertNull(context.getStartupFailure());
            assertEquals(0, context.getBeansOfType(MinioClient.class).size());
            var build = context.getBean(MinioBuildService.class);
            String key = build.buildObjectKey(UploadType.MOVIE_BACKDROP, "backdrop.png", null);
            var upload = context.getBean(MinioWriteService.class).createPresignedImageUpload(key, "image/png");
            assertEquals(key, upload.objectKey());
            assertEquals(key, upload.formData().get("public_id"));
            assertEquals("https://api.cloudinary.com/v1_1/demo-cloud/image/upload", upload.uploadUrl());
            assertTrue(build.buildPublicUrl(key).startsWith("https://res.cloudinary.com/demo-cloud/"));
        });
    }
}
