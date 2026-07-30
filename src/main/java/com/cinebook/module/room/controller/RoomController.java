package com.cinebook.module.room.controller;

import com.cinebook.common.response.ApiSuccessResponse;
import com.cinebook.module.room.dto.request.RoomCreateRequest;
import com.cinebook.module.room.dto.response.RoomResponse;
import com.cinebook.module.room.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cinemas/{cinemaId}/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<List<RoomResponse>>> list(@PathVariable UUID cinemaId) {
        return ResponseEntity.ok(ApiSuccessResponse.<List<RoomResponse>>builder()
                .message("Get list rooms by cinema successful!")
                .data(roomService.listByCinema(cinemaId))
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<RoomResponse>> create(@PathVariable UUID cinemaId,
                                                                   @Valid @RequestBody RoomCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.<RoomResponse>builder()
                .message("Create room by cinema successful!")
                .data(roomService.create(cinemaId, request))
                .build());
    }
}
