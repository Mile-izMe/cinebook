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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

        try {
            for (UUID seatId : sorted) {
                String key = keyFactory.buildKey(showtimeId, seatId);
                Instant now = Instant.now();
                SeatLockValue newValue = new SeatLockValue(ownerId, seatId, null, now, now.plusSeconds(ttl));

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
    public void unlockSeats(UUID ownerId, UUID showtimeId, List<UUID> seatIds) {
        for (UUID seatId : seatIds) {
            String key = keyFactory.buildKey(showtimeId, seatId);
            Long result = seatLockRepository.safeUnLock(key, ownerId);
            if (result == 1) broadcaster.broadcastSeatReleased(showtimeId, seatId);
            // result == -1 <=> key exist but belong to another owner - skip,
            // No throw error to avoid 1 wrong seat -> Make batch fail unlock of user.
        }
    }

    // -----------------------------------------------------------
    // Verify Lock Owner (Before createBooking)
    // -----------------------------------------------------------
    public boolean isLockedByOwner(UUID ownerId, UUID showtimeId, UUID seatId) {
        String key = keyFactory.buildKey(showtimeId, seatId);
        SeatLockValue value = seatLockRepository.getLock(key);
        return value != null && ownerId.equals(value.ownerId());
    }

    /**
     * After createBooking (success)
     */
    public void releaseAfterBookingCreated(UUID ownerId, UUID showtimeId, List<UUID> seatIds) {
        unlockSeats(ownerId, showtimeId, seatIds);
    }

    // -----------------------------------------------------------
    // Get locked seats (FE realtime refresh seat map)
    // -----------------------------------------------------------
    public Set<UUID> getLockedSeatIds(UUID showtimeId) {
        return seatLockRepository.findLockedSeatIdsByShowtime(showtimeId);
    }

}
