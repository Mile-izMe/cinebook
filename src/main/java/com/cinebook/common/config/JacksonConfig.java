package com.cinebook.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Note: Register module to let Jackson know how to
        // read/write Instant fields (lockedAt, expiresAt) into JSON
        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }
}
