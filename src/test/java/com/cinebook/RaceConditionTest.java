package com.cinebook;

import com.cinebook.module.lock.service.LockLearningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
public class RaceConditionTest {

    @Autowired
    private LockLearningService lockLearningService;

    @Test
    public void testHoldBooking() throws InterruptedException {
        int numberOfThreads = 1000; // Simulate 1000 concurrent users

        // Create a ThreadPool to handle 1000 threads simultaneously
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        // Latch 1: The starting gun. Blocks all 1000 threads, waiting for the countdown from 1 -> 0
        CountDownLatch startLatch = new CountDownLatch(1);

        // Latch 2: The scoreboard. Waits for all 1000 threads to finish to tally the results
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);

        // Thread-safe counter to track successful bookings
        AtomicInteger successCount = new AtomicInteger(0);

        // Create fixed UUIDs for the target showtime and seat that everyone will compete for
        UUID targetShowtimeId = UUID.randomUUID();
        UUID targetSeatId = UUID.randomUUID();

        for (int i = 0; i < numberOfThreads; i++) {
            // Generate a unique UUID for each user thread
            final UUID userId = UUID.randomUUID();

            executorService.submit(() -> {
                try {
                    // All 1000 threads will be blocked here upon startup,
                    // holding their breath waiting for startLatch to reach 0
                    startLatch.await();

                    // 💥 ATTACK: Call the booking function concurrently
                    boolean isBooked = lockLearningService.holdBooking(targetShowtimeId, targetSeatId, userId);

                    if (isBooked) {
                        successCount.incrementAndGet();
                        System.out.println("User " + userId + " successfully grabbed the ticket!");
                    }
                } catch (Exception e) {
                    // Catch exceptions if the booking fails or crashes
                    e.printStackTrace();
                } finally {
                    // Signal that this thread has finished its execution
                    endLatch.countDown();
                }
            });
        }

        System.out.println("Get ready...");
        // Wait 1 second to ensure all 1000 threads are fully initialized and in position
        Thread.sleep(1000);

        System.out.println("GO!!!");
        // BANG! startLatch counts down to 0, opening the gates for 1000 threads to hit the API simultaneously
        startLatch.countDown();

        // Wait for all 1000 threads to finish processing before asserting the result
        endLatch.await();
        executorService.shutdown();

        System.out.println("Test completed. Total tickets successfully sold for the target seat: " + successCount.get());
    }
}