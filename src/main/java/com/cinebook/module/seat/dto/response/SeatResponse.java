package com.cinebook.module.seat.dto.response;

import com.cinebook.module.seat.entity.SeatType;

import java.util.UUID;

public record SeatResponse(
        UUID id,
        UUID roomId,
        String row,
        Integer number,
        String label,
        SeatType seatType
) {
}