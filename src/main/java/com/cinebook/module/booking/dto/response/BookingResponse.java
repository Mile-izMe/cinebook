package com.cinebook.module.booking.dto.response;

import com.cinebook.module.booking.entity.BookingStatus;
import com.cinebook.module.booking.entity.SeatInformation;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record BookingResponse(
        UUID bookingId,
        String movie,
        String cinema,
        String address,
        String room,
        Instant showtime,
        List<String> seats,
        Integer totalPrice,
        BookingStatus status,
        Instant bookingTime,
        Instant expiresAt
) {
}