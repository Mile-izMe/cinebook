package com.cinebook.module.booking.repository;

import com.cinebook.module.booking.entity.Booking;
import com.cinebook.module.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByBookingCode(String bookingCode);

    List<Booking> findAllByStatusAndExpiresAtBefore(BookingStatus status, Instant time);
}
