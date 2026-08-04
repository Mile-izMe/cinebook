package com.cinebook.module.booking.entity;

import java.util.List;

public record BookingSnapshot(
        String movieName,
        String posterUrl,
        String cinemaName,
        String cinemaAddress,
        String roomName,
        String startTime,
        String format,
        List<String> seats
) {
}