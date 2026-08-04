package com.cinebook.module.booking.controller;

import com.cinebook.common.response.ApiSuccessResponse;
import com.cinebook.common.security.CustomerUserDetails;
import com.cinebook.common.util.CursorPageResponse;
import com.cinebook.module.booking.dto.request.BookingCreateRequest;
import com.cinebook.module.booking.dto.response.BookingResponse;
import com.cinebook.module.booking.dto.response.BookingSummaryResponse;
import com.cinebook.module.booking.service.BookingService;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // ===================== MEMBER ==========================
    // ---- Create Booking ----
    @PostMapping("/api/bookings")
    public ResponseEntity<ApiSuccessResponse<BookingResponse>> create(
            @Valid @RequestBody BookingCreateRequest request,
            @Nullable @AuthenticationPrincipal CustomerUserDetails userDetails) {
        UUID userId = (userDetails != null) ? userDetails.getUserId() : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.<BookingResponse>builder()
                .message("Create booking successful")
                .data(bookingService.createBooking(userId, request))
                .build());
    }


    // ---- Booking histories with cursor pagination ----
    @GetMapping("/me")
    public ResponseEntity<ApiSuccessResponse<List<BookingSummaryResponse>>> getHistories(
            @AuthenticationPrincipal CustomerUserDetails userDetails,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        int safeLimit = Math.min(limit, 50);
        CursorPageResponse<BookingSummaryResponse> page = bookingService.getBookingHistories(userDetails.getUserId(), cursor, safeLimit);

        return ResponseEntity.ok(ApiSuccessResponse.ofCursorPage(page, safeLimit, "Get booking histories successful"));
    }


    // ---- Booking detail ----
    @GetMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<BookingResponse>> getDetail(
            @AuthenticationPrincipal CustomerUserDetails userDetails,
            @PathVariable UUID id
    ) {
        BookingResponse detail = bookingService.getBookingDetail(userDetails.getUserId(), id);

        ApiSuccessResponse<BookingResponse> response = ApiSuccessResponse.<BookingResponse>builder()
                .message("Get booking detail successful")
                .data(detail)
                .build();

        return ResponseEntity.ok(response);
    }

    // ---- Cancel Booking ----
    @PostMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(
            @AuthenticationPrincipal CustomerUserDetails userDetails,
            @PathVariable UUID id
    ) {
        bookingService.cancelBooking(userDetails.getUserId(), id);
        return ResponseEntity.noContent().build();
    }
    // ===================== END ==========================

    // ===================== GUEST ========================
    // ---- Lookup Booking ----
    @GetMapping("/guest/lookup")
    public ResponseEntity<ApiSuccessResponse<BookingResponse>> lookupGuestBooking(
            @RequestParam String bookingCode, @RequestParam String email) {
        return ResponseEntity.ok(ApiSuccessResponse.<BookingResponse>builder()
                .message("Lookup booking successful")
                .data(bookingService.lookupByCodeAndEmail(bookingCode, email))
                .build());
    }

    @PostMapping("/guest/cancel")
    public ResponseEntity<Void> cancelBooking(
            @RequestBody String bookingCode,
            @RequestBody String email
    ) {
        bookingService.cancelGuestBooking(bookingCode, email);
        return ResponseEntity.noContent().build();
    }
    // ===================== END ==========================
}
