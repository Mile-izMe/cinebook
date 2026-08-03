package com.cinebook.module.booking.dto.response;

import com.cinebook.module.booking.entity.BookingStatus;
import com.cinebook.module.booking.entity.SeatInformation;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BookingSummaryResponse(
        UUID bookingId,
        String movieName,
        String posterUrl,
        List<SeatInformation> seats,
        Instant showtimeStart,
        Integer totalPrice,
        BookingStatus status
) {
}