package com.cinebook.module.seatlock.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.module.seatlock.config.SeatLockProperties;
import com.cinebook.module.seatlock.model.SeatLockValue;
import com.cinebook.module.seatlock.repository.SeatLockRepository;
import com.cinebook.module.seatlock.repository.impl.factory.SeatLockKeyFactory;
import com.cinebook.module.seatlock.websocket.SeatMapBroadcaster;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SeatLockService {

    private final SeatLockRepository seatLockRepository;
    private final SeatLockKeyFactory keyFactory;
    private final SeatLockProperties properties;
    private final SeatMapBroadcaster broadcaster;

    // -----------------------------------------------------------
    // Lock Seat - Validate Seat + Redis Lock
    // -----------------------------------------------------------
    public List<SeatLockValue> lockSeats(UUID ownerId, UUID showtimeId, List<UUID> seatIds) {
        // Arrange lock order - prevent deadlock between 2 request lock many chairs
        // opposite order (e.g: request A lock [1,2], request B lock [2,1] at the same time)
        List<UUID> sorted = seatIds.stream().sorted().toList();

        List<SeatLockValue> results = new ArrayList<>();
        List<UUID> acquiredByThisCall = new ArrayList<>();

        long ttl = properties.getTtlSeconds();
        long maxHoldMinutes = properties.getMaxHoldMinutes();

        try {
            for (UUID seatId : sorted) {
                String key = keyFactory.buildKey(showtimeId, seatId);
                Instant now = Instant.now();
                String lockToken = UUID.randomUUID().toString();
                Instant expiresAt = now.plusSeconds(ttl);
                Instant maxExpiresAt = now.plus(maxHoldMinutes, ChronoUnit.MINUTES);
                SeatLockValue newValue = new SeatLockValue(
                        ownerId,
                        seatId,
                        lockToken,
                        null,
                        now,
                        expiresAt,
                        maxExpiresAt
                );

                boolean acquired = seatLockRepository.acquireLock(key, newValue, Duration.ofSeconds(ttl));

                if (acquired) {
                    broadcaster.broadcastSeatLocked(showtimeId, seatId);
                    acquiredByThisCall.add(seatId);
                    results.add(newValue);
                    continue;
                }

                // Key exists - Check Idempotency
                // if the owner use this lock, no error.
                SeatLockValue existing = seatLockRepository.getLock(key);
                if (existing != null && ownerId.equals(existing.ownerId())) {
                    results.add(existing); // idempotent - return current lock, no extend TTL
                } else {
                    throw new CinebookException(ErrorCode.SEAT_ALREADY_LOCKED,
                            "Chair " + seatId + " currently hold by other person");
                }
            }
        } catch (CinebookException ex) {
            // All-or-nothing: rollback all chairs that had successes this request
            for (UUID seatId : acquiredByThisCall) {
                seatLockRepository.deleteLock(keyFactory.buildKey(showtimeId, seatId));
                broadcaster.broadcastSeatReleased(showtimeId, seatId);
            }
            throw ex;
        }

        return results;
    }

    // -----------------------------------------------------------
    // Unlock (Proactive unlock chair)
    // -----------------------------------------------------------
    public void unlockSeats(UUID showtimeId, Map<UUID, String> seatTokens) {
        for (Map.Entry<UUID, String> entry : seatTokens.entrySet()) {
            UUID seatId = entry.getKey();
            String lockToken = entry.getValue();
            String key = keyFactory.buildKey(showtimeId, seatId);
            Long result = seatLockRepository.safeUnLock(key, lockToken);

            if (result != null && result == 1L) {
                broadcaster.broadcastSeatReleased(showtimeId, seatId);
            }
            // result == -1 <=> key exist but belong to another owner - skip,
            // No throw error to avoid 1 wrong seat -> Make batch fail unlock of user.
        }
    }

    // -----------------------------------------------------------
    // Extend TTL (Client-side heartbeat)
    // -----------------------------------------------------------
    public String extendLockTtl(UUID showtimeId, UUID seatId, String lockToken) {
        String key = keyFactory.buildKey(showtimeId, seatId);
        String result = seatLockRepository.extendLock(key, lockToken, properties.getTtlSeconds());

        return switch (result) {
            case "EXPIRED" -> throw new CinebookException(ErrorCode.SEAT_LOCK_EXPIRED);
            case "TOKEN_MISMATCH" -> throw new CinebookException(ErrorCode.SEAT_LOCK_NOT_OWNED);
            case "MAX_HOLD_REACHED" -> throw new CinebookException(ErrorCode.SEAT_LOCK_MAX_HOLD_REACHED);
            default -> result; // "EXTENDED:123"
        };
    }

    // -----------------------------------------------------------
    // Verify Lock Owner (Before createBooking)
    // -----------------------------------------------------------
    public boolean isLockedByOwnerToken(UUID showtimeId, UUID seatId, String lockToken) {
        String key = keyFactory.buildKey(showtimeId, seatId);
        SeatLockValue value = seatLockRepository.getLock(key);
        return value != null && lockToken.equals(value.lockToken());
    }

    /**
     * After createBooking (success)
     */
    public void releaseAfterBookingCreated(UUID showtimeId, Map<UUID, String> seatTokens) {
        unlockSeats(showtimeId, seatTokens);
    }

    // -----------------------------------------------------------
    // Get locked seats (FE realtime refresh seat map)
    // -----------------------------------------------------------
    public Set<UUID> getLockedSeatIds(UUID showtimeId) {
        return seatLockRepository.findLockedSeatIdsByShowtime(showtimeId);
    }

}
