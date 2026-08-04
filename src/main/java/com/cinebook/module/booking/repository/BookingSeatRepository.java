package com.cinebook.module.booking.repository;

import com.cinebook.module.booking.entity.Booking;
import com.cinebook.module.booking.entity.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, UUID> {

    List<BookingSeat> findAllByBookingId(UUID bookingId);

    @Query("""
            SELECT bs FROM BookingSeat bs
            WHERE bs.showtimeId = :showtimeId
              AND bs.seat.id IN :seatIds
              AND bs.booking.status IN ('PENDING', 'PAID', 'USED')
            """)
    List<BookingSeat> findActiveByShowtimeAndSeats(java.util.UUID showtimeId, List<UUID> seatIds);
}
