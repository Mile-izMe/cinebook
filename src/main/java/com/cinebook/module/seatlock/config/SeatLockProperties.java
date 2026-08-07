package com.cinebook.module.seatlock.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class SeatLockProperties {

    @Value("${app.seat-lock.ttl-seconds}")
    private long ttlSeconds;

    @Value("${app.seat-lock.max-hold-minutes}")
    private long maxHoldMinutes;

    private String keyPrefix = "lock:seat:";
}
