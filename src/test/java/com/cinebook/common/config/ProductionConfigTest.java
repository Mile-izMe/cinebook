package com.cinebook.common.config;

import com.cinebook.common.security.SecurityConfig;
import com.cinebook.module.payment.gateway.MockPaymentGateway;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ProductionConfigTest {
    @Test void redisUsesTlsAndBoundedPools() {
        var config = new RedisConfig();
        ReflectionTestUtils.setField(config, "redisHost", "demo.upstash.io");
        ReflectionTestUtils.setField(config, "redisPort", 6379);
        ReflectionTestUtils.setField(config, "redisPassword", "test-password");
        ReflectionTestUtils.setField(config, "redisUsername", "default");
        ReflectionTestUtils.setField(config, "redisSsl", true);
        var server = config.clientConfig().useSingleServer();
        assertEquals("rediss://demo.upstash.io:6379", server.getAddress());
        assertEquals("default", server.getUsername());
        assertEquals(4, server.getConnectionPoolSize());
        assertEquals(1, server.getConnectionMinimumIdleSize());
        ReflectionTestUtils.setField(config, "redisSsl", false);
        assertEquals("redis://demo.upstash.io:6379", config.clientConfig().useSingleServer().getAddress());
    }
    @Test void corsUsesConfiguredFrontendOrigin() {
        var config = new SecurityConfig(null, null, null, null);
        ReflectionTestUtils.setField(config, "frontendUrl", "https://demo.vercel.app");
        var cors = config.corsConfigurationSource().getCorsConfiguration(new MockHttpServletRequest());
        assertNotNull(cors);
        assertEquals(List.of("https://demo.vercel.app"), cors.getAllowedOrigins());
        assertNull(cors.checkOrigin("https://unrelated.example"));
    }
    @Test void paymentUrlUsesFrontendAndRemovesTrailingSlash() {
        var gateway = new MockPaymentGateway();
        ReflectionTestUtils.setField(gateway, "frontendUrl", "https://demo.vercel.app/");
        var result = gateway.createPayment(UUID.randomUUID(), 80000, UUID.randomUUID());
        assertTrue(result.paymentUrl().startsWith("https://demo.vercel.app/mock-payment?"));
    }
}
