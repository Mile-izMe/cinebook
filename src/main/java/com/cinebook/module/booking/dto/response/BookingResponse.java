package com.cinebook.module.booking.dto.response;

import com.cinebook.module.booking.entity.BookingStatus;
import com.cinebook.module.booking.entity.SeatInformation;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookingResponse(
        UUID bookingId,
        String movie,
        String cinema,
        String room,
        Instant showtime,
        List<SeatInformation> seats,
        Integer totalPrice,
        BookingStatus status,
        Instant bookingTime
) {
}