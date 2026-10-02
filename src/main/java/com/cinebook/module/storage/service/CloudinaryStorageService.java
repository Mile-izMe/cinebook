package com.cinebook.module.storage.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.module.storage.dto.PresignUrlResponse;
import com.cinebook.module.storage.type.UploadType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/** Cloudinary adapter; existing MinIO facades and object-key contracts stay intact. */
@Service
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "cloudinary")
public class CloudinaryStorageService {
    private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageService.class);
    private static final long MAX_BYTES = 5 * 1024 * 1024;
    private static final Set<String> CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> FORMATS = Set.of("jpg", "png", "webp");
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;
    private final URI apiRoot;
    private final Clock clock;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public CloudinaryStorageService(@Value("${cloudinary.cloud-name}") String cloudName,
                                    @Value("${cloudinary.api-key}") String apiKey,
                                    @Value("${cloudinary.api-secret}") String apiSecret) {
        this(cloudName, apiKey, apiSecret, URI.create("https://api.cloudinary.com/v1_1/"), Clock.systemUTC());
    }

    CloudinaryStorageService(String cloudName, String apiKey, String apiSecret, URI apiRoot, Clock clock) {
        if (cloudName == null || !cloudName.matches("[a-zA-Z0-9_-]+")
                || apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            throw new IllegalStateException("Cloudinary requires CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY and CLOUDINARY_API_SECRET");
        }
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.apiRoot = apiRoot;
        this.clock = clock;
    }

    public static boolean isCloudinaryKey(String key) {
        return key != null && key.startsWith("cloudinary/");
    }

    public String buildObjectKey(UploadType type, UUID userId) {
        String id = UUID.randomUUID().toString();
        return switch (type) {
            case AVATAR -> {
                if (userId == null) throw new IllegalArgumentException("Avatar requires a user");
                yield "cloudinary/avatars/" + userId + "/" + id;
            }
            case MOVIE_POSTER -> "cloudinary/uploads/posters/" + id;
            case MOVIE_BACKDROP -> "cloudinary/uploads/backdrops/" + id;
        };
    }

    public PresignUrlResponse createUpload(String key, String contentType) {
        validateKey(key);
        if (!CONTENT_TYPES.contains(contentType == null ? "" : contentType)) {
            throw new CinebookException(ErrorCode.INVALID_FILE_TYPE);
        }
        Map<String, String> params = new TreeMap<>();
        params.put("public_id", key);
        params.put("timestamp", Long.toString(clock.instant().getEpochSecond()));
        params.put("overwrite", "false");
        params.put("allowed_formats", "jpg,png,webp");
        params.put("signature", sign(params));
        params.put("api_key", apiKey);
        // Cloudinary upload signatures are valid for one hour.
        return new PresignUrlResponse(apiRoot.resolve(cloudName + "/image/upload").toString(), params, key, 3600);
    }

    private String sign(Map<String, String> params) {
        String canonical = new TreeMap<>(params).entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1")
                    .digest((canonical + apiSecret).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot sign Cloudinary upload", e);
        }
    }

    public boolean objectExists(String key) {
        validateKey(key);
        HttpRequest request = HttpRequest.newBuilder(apiRoot.resolve(cloudName + "/resources/image/upload/" + encode(key)))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Basic " + Base64.getEncoder()
                        .encodeToString((apiKey + ":" + apiSecret).getBytes(StandardCharsets.UTF_8)))
                .GET().build();
        HttpResponse<String> response = send(request);
        if (response.statusCode() == 404) return false;
        if (response.statusCode() != 200) throw new CinebookException(ErrorCode.FILE_UPLOAD_FAILED);
        try {
            JsonNode asset = mapper.readTree(response.body());
            if (!key.equals(asset.path("public_id").asText())
                    || !"image".equals(asset.path("resource_type").asText())
                    || !"upload".equals(asset.path("type").asText())
                    || !FORMATS.contains(asset.path("format").asText())) {
                throw new CinebookException(ErrorCode.INVALID_FILE_TYPE);
            }
            if (!asset.path("bytes").isIntegralNumber() || asset.path("bytes").asLong() < 1) {
                throw new CinebookException(ErrorCode.FILE_UPLOAD_FAILED);
            }
            if (asset.path("bytes").asLong() > MAX_BYTES) throw new CinebookException(ErrorCode.FILE_TOO_LARGE);
            return true;
        } catch (CinebookException e) {
            throw e;
        } catch (Exception e) {
            throw new CinebookException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public String buildPublicUrl(String key) {
        validateKey(key);
        String path = java.util.Arrays.stream(key.split("/")).map(CloudinaryStorageService::encode)
                .collect(Collectors.joining("/"));
        return "https://res.cloudinary.com/" + cloudName + "/image/upload/" + path;
    }

    public void deleteObject(String key) {
        validateKey(key);
        Map<String, String> params = new TreeMap<>();
        params.put("public_id", key);
        params.put("timestamp", Long.toString(clock.instant().getEpochSecond()));
        params.put("invalidate", "true");
        params.put("signature", sign(params));
        params.put("api_key", apiKey);
        String body = params.entrySet().stream().map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
        try {
            HttpResponse<String> response = send(HttpRequest.newBuilder(apiRoot.resolve(cloudName + "/image/destroy"))
                    .timeout(Duration.ofSeconds(15)).header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build());
            if (response.statusCode() != 200) log.warn("Cloudinary cleanup failed with status {}", response.statusCode());
        } catch (CinebookException e) {
            log.warn("Cloudinary cleanup failed; retaining old asset {}", key);
        }
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CinebookException(ErrorCode.FILE_UPLOAD_FAILED);
        } catch (Exception e) {
            throw new CinebookException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static void validateKey(String key) {
        if (key == null || !key.matches("cloudinary/(?:avatars/[0-9a-f-]{36}|uploads/(?:posters|backdrops))/[0-9a-f-]{36}")) {
            throw new CinebookException(ErrorCode.VALIDATION_ERROR, "Invalid uploaded image key");
        }
    }
}
