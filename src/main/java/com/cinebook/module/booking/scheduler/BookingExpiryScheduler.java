package com.cinebook.module.booking.scheduler;

import com.cinebook.module.booking.entity.Booking;
import com.cinebook.module.booking.entity.BookingStatus;
import com.cinebook.module.booking.repository.BookingRepository;
import com.cinebook.module.booking.repository.BookingSeatRepository;
import com.cinebook.module.booking.service.BookingService;
import com.cinebook.module.payment.entity.PaymentStatus;
import com.cinebook.module.payment.repository.PaymentRepository;
import com.cinebook.module.seatlock.service.SeatLockService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingExpiryScheduler.class);

    private final BookingService bookingService;

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PaymentRepository paymentRepository;
    private final SeatLockService seatLockService;

    @Scheduled(fixedRate = 60_000) // Every 1 minute
    public void expireOverdueBookings() {
        List<Booking> expired = bookingRepository.findAllByStatusAndExpiresAtBefore(
                BookingStatus.PENDING, Instant.now());

        for (Booking booking : expired) {
            expireOne(booking.getId()); // Each booking 1 transaction, prevent 1 error rollback the whole batch
        }
        if (!expired.isEmpty()) {
            log.info("Expired {} overdue bookings", expired.size());
        }
    }

    @Transactional
    public void expireOne(UUID bookingId) {
        Booking booking = bookingService.findOrThrow(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING) return;

        booking.setStatus(BookingStatus.EXPIRED);
        bookingRepository.save(booking);

        paymentRepository.findAllByBookingIdOrderByCreatedAtDesc(bookingId).stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .forEach(p -> {
                    p.setStatus(PaymentStatus.CANCELLED);
                    paymentRepository.save(p);
                });

        List<UUID> seatIds = bookingSeatRepository.findAllByBookingId(bookingId).stream()
                .map(bs -> bs.getSeat().getId()).toList();
        UUID showtimeId = booking.getShowtime().getId();

        seatLockService.forceUnlockSeats(showtimeId, seatIds);
    }
}
