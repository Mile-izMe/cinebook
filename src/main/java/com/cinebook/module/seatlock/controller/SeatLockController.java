package com.cinebook.module.seatlock.controller;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.common.response.ApiSuccessResponse;
import com.cinebook.common.security.CustomerUserDetails;
import com.cinebook.module.seatlock.dto.request.SeatLockRequest;
import com.cinebook.module.seatlock.dto.request.SeatUnlockRequest;
import com.cinebook.module.seatlock.dto.response.SeatLockResponse;
import com.cinebook.module.seatlock.model.SeatLockValue;
import com.cinebook.module.seatlock.service.SeatLockService;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/seat-locks")
@RequiredArgsConstructor
public class SeatLockController {

    private final SeatLockService seatLockService;

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<List<SeatLockResponse>>> lockSeat(
            @Valid @RequestBody SeatLockRequest request,
            @Nullable @AuthenticationPrincipal CustomerUserDetails userDetails,
            HttpServletRequest httpRequest
    ) {

        String ownerId = (userDetails != null)
                ? userDetails.getUserId().toString()
                : httpRequest.getHeader("X-Device-ID");

        if (ownerId == null || ownerId.isBlank()) {
            throw new CinebookException(ErrorCode.INTERNAL_SERVER_ERROR, "Missing authentication or X-Device-ID header");
        }

        List<SeatLockValue> locked = seatLockService.lockSeats(ownerId, request.showtimeId(), request.seatIds());

        List<SeatLockResponse> response = locked.stream()
                .map(v -> new SeatLockResponse(v.seatId(), v.lockToken(), v.expiresAt()))
                .toList();

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.<List<SeatLockResponse>>builder()
                .message("Hold seats success!")
                .data(response)
                .build());
    }

    @DeleteMapping
    public ResponseEntity<Void> unlock(
            @Valid @RequestBody SeatUnlockRequest request) {
        seatLockService.unlockSeats(request.showtimeId(), request.seatTokens());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/showtimes/{showtimeId}")
    public ResponseEntity<ApiSuccessResponse<Set<UUID>>> getLockedSeats(@PathVariable UUID showtimeId) {
        return ResponseEntity.ok(ApiSuccessResponse.<Set<UUID>>builder()
                .message("Get list hold seats success!")
                .data(seatLockService.getLockedSeatIds(showtimeId))
                .build());
    }
}
