package com.cinebook.module.booking.repository;

import com.cinebook.module.booking.entity.Booking;
import com.cinebook.module.booking.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<BookingSeat> findAllByBookingId(UUID bookingId);
}
