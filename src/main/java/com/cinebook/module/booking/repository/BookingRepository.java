package com.cinebook.module.booking.repository;

import com.cinebook.module.booking.entity.Booking;
import com.cinebook.module.booking.entity.BookingStatus;
import com.cinebook.module.user.dto.response.UserStatsResponse;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByBookingCode(String bookingCode);

    List<Booking> findAllByStatusAndExpiresAtBefore(BookingStatus status, Instant time);

    @Query("SELECT b.snapshot, b.totalPrice FROM Booking b WHERE b.user.id = :userId AND b.status = :status")
    List<Object[]> getRawBookingData(@Param("userId") UUID userId, @Param("status") BookingStatus status);
}
