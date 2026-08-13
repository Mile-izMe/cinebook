package com.cinebook.module.booking.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record TicketResponse(
        String bookingCode,
        String movieTitle,
        String format,
        Integer duration,

        String cinemaName,
        String cinemaAddress,
        String roomName,
        LocalDateTime showtime,

        List<String> seats,
        int totalPrice,
        String paymentMethod
) {
}
