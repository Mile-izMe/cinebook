package com.cinebook.module.booking.mapper;

import com.cinebook.module.booking.dto.response.BookingResponse;
import com.cinebook.module.booking.dto.response.TicketResponse;
import com.cinebook.module.booking.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "movie", source = "booking.showtime.movie.title")
    @Mapping(target = "cinema", source = "booking.showtime.room.cinema.name")
    @Mapping(target = "address", source = "booking.showtime.room.cinema.address")
    @Mapping(target = "room", source = "booking.showtime.room.name")
    @Mapping(
            target = "showtime",
            expression = "java(booking.getShowtime().getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant())"
    )
    @Mapping(target = "seats", source = "seatLabels")
    @Mapping(target = "totalPrice", source = "booking.totalPrice")
    @Mapping(target = "status", source = "booking.status")
    @Mapping(target = "bookingTime", source = "booking.bookingTime")
    BookingResponse toResponse(Booking booking, List<String> seatLabels);

    @Mapping(target = "bookingCode", source = "booking.bookingCode")
    @Mapping(target = "totalPrice", source = "booking.totalPrice")
    @Mapping(target = "paymentMethod", source = "paymentMethod")
    @Mapping(target = "movieTitle", source = "booking.snapshot.movieName")
    @Mapping(target = "format", source = "booking.snapshot.format")
    @Mapping(target = "cinemaName", source = "booking.snapshot.cinemaName")
    @Mapping(target = "cinemaAddress", source = "booking.snapshot.cinemaAddress")
    @Mapping(target = "roomName", source = "booking.snapshot.roomName")
    @Mapping(target = "showtime", source = "booking.snapshot.startTime")
    @Mapping(target = "seats", source = "booking.snapshot.seats")
    TicketResponse toTicketResponse(Booking booking, String paymentMethod);
}
