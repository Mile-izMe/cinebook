package com.cinebook.module.room.dto.response;

import com.cinebook.module.room.entity.RoomStatus;
import com.cinebook.module.room.entity.RoomType;

import java.util.UUID;

public record RoomResponse(
        UUID id, String name, Integer capacity,
        RoomType roomType, RoomStatus status, UUID cinemaId,
        String cinemaName
) {
}