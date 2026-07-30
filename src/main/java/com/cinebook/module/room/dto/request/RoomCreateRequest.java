package com.cinebook.module.room.dto.request;

import com.cinebook.module.room.entity.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RoomCreateRequest(
        @NotBlank(message = "Room name must not be empty!")
        String name,

        @NotNull @Positive(message = "Capacity must be larger than 0!")
        Integer capacity,

        @NotNull(message = "Room type must not be empty!")
        RoomType roomType
) {
}