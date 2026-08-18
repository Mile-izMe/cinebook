package com.cinebook;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.module.seatlock.model.SeatLockValue;
import com.cinebook.module.seatlock.repository.SeatLockRepository;
import com.cinebook.module.seatlock.repository.impl.factory.SeatLockKeyFactory;
import com.cinebook.module.seatlock.service.SeatLockService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class SeatLockIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SeatLockService seatLockService;

    @Autowired
    private SeatLockRepository seatLockRepository;

    @Autowired
    private SeatLockKeyFactory keyFactory;

    private final UUID showtimeId = UUID.randomUUID();
    private final UUID seatA1 = UUID.randomUUID();
    private final UUID seatA2 = UUID.randomUUID();
    private final UUID seatB1 = UUID.randomUUID();

    // -------------------------------------------------------------------------
    // TC-01: Lock 1 seat success
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("TC-01: Lock 1 seat success - Verify API response & Redis DB")
    void lockSingleSeat_success() throws Exception {
        String requestBody = """
                {
                    "showtimeId": "%s",
                    "seatIds": ["%s"]
                }
                """.formatted(showtimeId, seatA1);

        // 1. GỌI API (API Verification)
        MvcResult result = mockMvc.perform(post("/api/seat-locks")
                        .header("X-Device-ID", "user-A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated()) // Controller return HttpStatus.CREATED
                .andExpect(jsonPath("$.data[0].seatId").value(seatA1.toString()))
                .andExpect(jsonPath("$.data[0].lockToken").exists())
                .andExpect(jsonPath("$.data[0].expiresAt").exists())
                .andReturn();

        // Lấy token từ response JSON
        String responseJson = result.getResponse().getContentAsString();
        String lockTokenFromApi = JsonPath.read(responseJson, "$.data[0].lockToken");

        // 2. KIỂM TRA REDIS (State Verification)
        String key = keyFactory.buildKey(showtimeId, seatA1);
        SeatLockValue dbValue = seatLockRepository.getLock(key);

        assertThat(dbValue).isNotNull();
        assertThat(dbValue.ownerId()).isEqualTo("user-A");
        assertThat(dbValue.lockToken()).isEqualTo(lockTokenFromApi); // Token DB phải khớp token trả về cho user
    }

    // -------------------------------------------------------------------------
    // TC-02: User B intentionally lock User A's holding seat -> Error 409
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("TC-02: Different user tries to lock the same seat - Conflict 409")
    void lockSameSeat_byDifferentUser_fails() throws Exception {
        // User A lock by service
        List<SeatLockValue> userALocks = seatLockService.lockSeats("user-A", showtimeId, List.of(seatA1));
        String tokenA = userALocks.get(0).lockToken();

        // User B call API to lock A1's seat
        String requestBodyUserB = """
                {
                    "showtimeId": "%s",
                    "seatIds": ["%s"]
                }
                """.formatted(showtimeId, seatA1);

        // Verify API handle right Exception (CinebookException - SEAT_ALREADY_LOCKED)
        mockMvc.perform(post("/api/seat-locks")
                        .header("X-Device-ID", "user-B")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyUserB))
                .andExpect(result -> {
                    Exception ex = result.getResolvedException();
                    assertThat(ex).isInstanceOf(CinebookException.class);
                    assertThat(((CinebookException) ex).getErrorCode()).isEqualTo(ErrorCode.SEAT_ALREADY_LOCKED);
                });

        // Verify DB: Seat still belongs User A
        String key = keyFactory.buildKey(showtimeId, seatA1);
        SeatLockValue dbValue = seatLockRepository.getLock(key);
        assertThat(dbValue.ownerId()).isEqualTo("user-A");
        assertThat(dbValue.lockToken()).isEqualTo(tokenA);
    }

    // -------------------------------------------------------------------------
    // TC-03: Lock many seats success
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("TC-03: Lock multiple seats successfully")
    void lockMultipleSeats_success() throws Exception {
        String requestBody = """
                {
                    "showtimeId": "%s",
                    "seatIds": ["%s", "%s", "%s"]
                }
                """.formatted(showtimeId, seatA1, seatA2, seatB1);

        mockMvc.perform(post("/api/seat-locks")
                        .header("X-Device-ID", "user-A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.length()").value(3)); // Trả về đủ 3 ghế

        // Verify Redis has 3 keys
        assertThat(seatLockRepository.getLock(keyFactory.buildKey(showtimeId, seatA1))).isNotNull();
        assertThat(seatLockRepository.getLock(keyFactory.buildKey(showtimeId, seatA2))).isNotNull();
        assertThat(seatLockRepository.getLock(keyFactory.buildKey(showtimeId, seatB1))).isNotNull();
    }

    // -------------------------------------------------------------------------
    // TC-04: Atomicity - Lock 3 seats but 1 seat taken -> Rollback
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("TC-04: Atomicity - Rollback all if one seat is conflicted")
    void lockMultipleSeats_partialConflict_rollsBackAll() throws Exception {
        // User B take A2 first
        seatLockService.lockSeats("user-B", showtimeId, List.of(seatA2));

        // User A try to take group seats: [A1, A2, B1]
        String requestBody = """
                {
                    "showtimeId": "%s",
                    "seatIds": ["%s", "%s", "%s"]
                }
                """.formatted(showtimeId, seatA1, seatA2, seatB1);

        // Verify API
        mockMvc.perform(post("/api/seat-locks")
                        .header("X-Device-ID", "user-A")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(CinebookException.class));

        // Verify Redis (IMPORTANT): A1 & B1 not stored in DB
        assertThat(seatLockRepository.getLock(keyFactory.buildKey(showtimeId, seatA1))).isNull();
        assertThat(seatLockRepository.getLock(keyFactory.buildKey(showtimeId, seatB1))).isNull();

        // A2 still belong to User B
        assertThat(seatLockRepository.getLock(keyFactory.buildKey(showtimeId, seatA2)).ownerId()).isEqualTo("user-B");
    }

    // -------------------------------------------------------------------------
    // TC-05: Unlock seat by owner token -> Redis Key deleted
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("TC-05: Unlock seat with correct token - Redis key deleted")
    void unlock_byOwner_deletesKey() throws Exception {
        // User A lock seat and get token
        List<SeatLockValue> locks = seatLockService.lockSeats("user-A", showtimeId, List.of(seatA1));
        String realToken = locks.get(0).lockToken();

        // API DELETE /api/seat-locks
        String requestBody = """
                {
                    "showtimeId": "%s",
                    "seatTokens": {
                        "%s": "%s"
                    }
                }
                """.formatted(showtimeId, seatA1, realToken);

        mockMvc.perform(delete("/api/seat-locks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent()); // HTTP 204

        // Verify DB: Key removed
        String key = keyFactory.buildKey(showtimeId, seatA1);
        assertThat(seatLockRepository.getLock(key)).isNull();
    }

    // -------------------------------------------------------------------------
    // TC-06: Mistaken token / fake Token to unlock -> unlock command cancelled, lock survive
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("TC-06: Unlock with wrong token - Silently rejected, lock survives")
    void unlock_byNonOwner_isRejected() throws Exception {
        // User A lock seat
        List<SeatLockValue> locks = seatLockService.lockSeats("user-A", showtimeId, List.of(seatA1));
        String realToken = locks.get(0).lockToken();

        // Try to break rule and unlock using 1 token fake (random)
        String fakeToken = UUID.randomUUID().toString();
        String requestBody = """
                {
                    "showtimeId": "%s",
                    "seatTokens": {
                        "%s": "%s"
                    }
                }
                """.formatted(showtimeId, seatA1, fakeToken);

        // API unlock pass wrong key not to failed whole batch, return 204
        mockMvc.perform(delete("/api/seat-locks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());

        // Verify DB: Lock still belongs to User A (No mistaken deletion)
        String key = keyFactory.buildKey(showtimeId, seatA1);
        SeatLockValue dbValue = seatLockRepository.getLock(key);
        assertThat(dbValue).isNotNull();
        assertThat(dbValue.lockToken()).isEqualTo(realToken);
    }

    // -------------------------------------------------------------------------
    // TC-Concurrent: Test Race Condition by Java Thread
    // -------------------------------------------------------------------------
    @Test
    @DisplayName("Race condition: 100 thread lock the same seat - only 1 success")
    void concurrentLock_sameSeat_onlyOneSucceeds() throws InterruptedException {
        UUID contestedSeat = UUID.randomUUID();
        int threadCount = 100;
        ExecutorService pool = Executors.newFixedThreadPool(20);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final String userId = "user-" + i;
            pool.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    seatLockService.lockSeats(userId, showtimeId, List.of(contestedSeat));
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                    // expected for the 99 losers
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown(); // fire all threads at once
        doneLatch.await();
        pool.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
    }
}