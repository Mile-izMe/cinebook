package com.cinebook.module.storage.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.module.storage.type.UploadType;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class CloudinaryStorageServiceTest {
    private HttpServer server;
    private CloudinaryStorageService storage;
    private final String key = "cloudinary/uploads/posters/11111111-1111-1111-1111-111111111111";
    private int status = 200;
    private String asset;
    private final AtomicReference<String> requestBody = new AtomicReference<>();

    @BeforeEach void setUp() throws Exception {
        asset = "{\"public_id\":\"" + key + "\",\"resource_type\":\"image\",\"type\":\"upload\",\"format\":\"png\",\"bytes\":1024}";
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] body = asset.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        storage = new CloudinaryStorageService("test-cloud", "test-key", "test-secret",
                URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/"),
                Clock.fixed(Instant.ofEpochSecond(1700000000), ZoneOffset.UTC));
    }

    @AfterEach void stop() { server.stop(0); }

    @Test void uploadIsSignedAndDoesNotExposeTheSecret() {
        var response = storage.createUpload(key, "image/png");
        assertEquals(key, response.formData().get("public_id"));
        assertEquals("1700000000", response.formData().get("timestamp"));
        assertEquals("false", response.formData().get("overwrite"));
        assertEquals("jpg,png,webp", response.formData().get("allowed_formats"));
        assertEquals(40, response.formData().get("signature").length());
        assertFalse(response.formData().containsValue("test-secret"));
        assertEquals(3600, response.expiresInSeconds());
        assertThrows(CinebookException.class, () -> storage.createUpload(key, "image/svg+xml"));
    }

    @Test void uploadedImageMustMatchKeyFormatAndSize() {
        assertTrue(storage.objectExists(key));
        asset = asset.replace("1024", "5242881");
        assertThrows(CinebookException.class, () -> storage.objectExists(key));
        asset = asset.replace("png", "svg");
        assertThrows(CinebookException.class, () -> storage.objectExists(key));
    }

    @Test void notFoundAndServiceFailureAreDifferent() {
        status = 404;
        assertFalse(storage.objectExists(key));
        status = 500;
        assertThrows(CinebookException.class, () -> storage.objectExists(key));
    }

    @Test void cannotReferenceForeignAssetsOrUseTraversal() {
        assertThrows(CinebookException.class, () -> storage.objectExists("someone-elses-image"));
        assertThrows(CinebookException.class, () -> storage.createUpload("cloudinary/../image", "image/png"));
        asset = asset.replace(key, "foreign-image");
        assertThrows(CinebookException.class, () -> storage.objectExists(key));
    }

    @Test void avatarReplacementGetsANewUrlAndUsesTheCorrectOwner() {
        UUID user = UUID.randomUUID();
        String first = storage.buildObjectKey(UploadType.AVATAR, user);
        String second = storage.buildObjectKey(UploadType.AVATAR, user);
        assertNotEquals(first, second);
        assertTrue(first.startsWith("cloudinary/avatars/" + user + "/"));
        assertTrue(storage.buildPublicUrl(first).startsWith("https://res.cloudinary.com/test-cloud/image/upload/"));
    }

    @Test void cleanupSignsDestroyAndInvalidatesTheCdn() {
        asset = "{\"result\":\"ok\"}";
        storage.deleteObject(key);
        assertTrue(requestBody.get().contains("invalidate=true"));
        assertTrue(requestBody.get().contains("signature="));
        assertFalse(requestBody.get().contains("test-secret"));
        status = 500;
        assertDoesNotThrow(() -> storage.deleteObject(key));
    }
}
