package com.cinebook;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

/**
 * Shared base for all 28 test cases. Uses REAL Postgres + Redis via
 * Testcontainers (not H2/embedded fakes) - Lua scripts, row locking
 * (FOR UPDATE), and JSONB columns don't behave the same on fakes.
 * <p>
 * RabbitMQ is mocked out (event publishers only) so tests don't need a
 * running broker; Payment/Booking logic under test doesn't depend on
 * consumers actually running.
 * <p>
 */
@Testcontainers
@SpringBootTest
public abstract class BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7").withExposedPorts(6379);

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    protected RedisTemplate<String, String> redisTemplate;

    // Mock RabbitMQ publishers - swap these class names for your actual
    // ReviewEventPublisher / MailEventPublisher / BookingConfirmedEventPublisher etc.
    // @MockBean protected com.cinebook.module.review.messaging.ReviewEventPublisher reviewEventPublisher;

    @BeforeEach
    void flushRedisBeforeEachTest() {
        // Every test starts with a clean Redis - seat locks from a previous
        // test must never leak into the next one.
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}