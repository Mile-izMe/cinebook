package com.cinebook.module.seatlock.repository;

import com.cinebook.module.seatlock.model.SeatLockValue;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

public interface SeatLockRepository {

    boolean acquireLock(String key, SeatLockValue value, Duration ttl);

    SeatLockValue getLock(String key);

    void deleteLock(String key);

    Long safeUnLock(String key, String lockToken);

    Set<UUID> findLockedSeatIdsByShowtime(UUID showtimeId);

    String extendLock(String key, String lockToken, long ttl);
}
