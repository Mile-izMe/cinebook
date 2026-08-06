package com.cinebook.module.seatlock.repository.impl.factory;

import com.cinebook.module.seatlock.config.SeatLockProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SeatLockKeyFactory {

    private final SeatLockProperties properties;

    public String buildKey(UUID showtimeId, UUID seatId) {
        return properties.getKeyPrefix() + showtimeId + ":" + seatId;
    }

    public String buildShowtimePattern(UUID showtimeId) {
        return properties.getKeyPrefix() + showtimeId + ":*";
    }
}
