package com.cinebook.module.seat.controller;

import com.cinebook.common.response.ApiSuccessResponse;
import com.cinebook.module.seat.dto.request.GenerateSeatRequest;
import com.cinebook.module.seat.dto.response.SeatResponse;
import com.cinebook.module.seat.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms/{roomId}/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<List<SeatResponse>>> list(@PathVariable UUID roomId) {
        return ResponseEntity.ok(ApiSuccessResponse.<List<SeatResponse>>builder()
                .message("Get list seats successful!")
                .data(seatService.listByRoom(roomId))
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/generate-seats")
    public ResponseEntity<ApiSuccessResponse<List<SeatResponse>>> generate(
            @PathVariable UUID roomId, @Valid @RequestBody GenerateSeatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.<List<SeatResponse>>builder()
                .message("Generate seats successful!")
                .data(seatService.generateSeats(roomId, request))
                .build());
    }
}
