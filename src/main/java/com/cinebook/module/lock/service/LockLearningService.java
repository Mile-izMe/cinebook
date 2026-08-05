package com.cinebook.module.lock.service;

import com.cinebook.module.lock.base.RedisLockBase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class LockLearningService {

    private final RedisLockBase redisLockBase; // FOR TESTING ONLY

    // -----------------------------------------------------------
    // REDIS BASE - TESTING TO LEARN RACE CONDITION (ONLY FOR TEST)
    // -----------------------------------------------------------
    public boolean holdBooking(UUID showtimeId, UUID seatId, UUID userId) {
        // Time start to request Key
        long startTime = System.currentTimeMillis();

        boolean isLocked = redisLockBase.tryLock(showtimeId, seatId, userId.toString(), Duration.ofSeconds(10));
        if (!isLocked) {
            log.warn("❌ User {} request key failed after {} ms", userId, (System.currentTimeMillis() - startTime));
            return false;
        }

        log.info("🔒 [LOCK ACQUIRED] - User: {} - Seat: {} - Time: {} ms",
                userId, seatId, System.currentTimeMillis());
        try {
            return doHoldBookingInDb(showtimeId, seatId, userId);

        } catch (Exception e) {
            throw new RuntimeException("Error holding seats", e);
        } finally {
            redisLockBase.unlock(showtimeId, seatId, userId.toString());
            long lockDuration = System.currentTimeMillis() - startTime;
            log.info("🔓 [LOCK RELEASED] - User: {} - Seat: {} - Time: {} ms - Total Held: {} ms",
                    userId, seatId, System.currentTimeMillis(), lockDuration);
        }
    }

    @Transactional
    public boolean doHoldBookingInDb(UUID showtimeId, UUID seatId, UUID userId) {
        // DB (Insert booking, insert booking_seat...)
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return true;
    }
    // -----------------------------------------------------------
    //                          END
    // -----------------------------------------------------------
}
