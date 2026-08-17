package com.cinebook.module.payment.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.module.booking.entity.Booking;
import com.cinebook.module.booking.entity.BookingStatus;
import com.cinebook.module.booking.repository.BookingSeatRepository;
import com.cinebook.module.booking.service.BookingService;
import com.cinebook.module.booking.validator.BookingStatusManager;
import com.cinebook.module.payment.dto.request.CreatePaymentRequest;
import com.cinebook.module.payment.dto.request.MockCallbackRequest;
import com.cinebook.module.payment.dto.response.CallbackPayload;
import com.cinebook.module.payment.dto.response.PaymentResponse;
import com.cinebook.module.payment.entity.Payment;
import com.cinebook.module.payment.entity.PaymentStatus;
import com.cinebook.module.payment.gateway.MockPaymentGateway;
import com.cinebook.module.payment.gateway.PaymentGateway;
import com.cinebook.module.payment.mapper.PaymentMapper;
import com.cinebook.module.payment.repository.PaymentRepository;
import com.cinebook.module.payment.validator.PaymentStatusManager;
import com.cinebook.module.seatlock.service.SeatLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingSeatRepository bookingSeatRepository;

    private final BookingService bookingService;
    private final SeatLockService seatLockService;

    private final PaymentGateway paymentGateway;
    private final PaymentMapper paymentMapper;
    private final PaymentStatusManager paymentStatusManager;
    private final BookingStatusManager bookingStatusManager;

    // -----------------------------------------------------------
    // Create Payment
    // -----------------------------------------------------------
    @Transactional
    public PaymentResponse createPayment(UUID userId, UUID bookingId, CreatePaymentRequest request) {
        Booking booking = bookingService.findOrThrow(bookingId);

        if (booking.getUser() != null) {
            // Order belong to authenticated User
            if (!booking.getUser().getId().equals(userId)) {
                throw new CinebookException(ErrorCode.BOOKING_ACCESS_DENIED, "You do not own this booking");
            }
        } else {
            // Order belong to Guest
            boolean isValidGuest = request.guestEmail() != null
                    && request.guestPhone() != null
                    && request.guestEmail().equalsIgnoreCase(booking.getGuestEmail())
                    && request.guestPhone().equals(booking.getGuestPhone());

            if (!isValidGuest) {
                throw new CinebookException(ErrorCode.BOOKING_ACCESS_DENIED, "Invalid guest credentials for this booking");
            }
        }

        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new CinebookException(ErrorCode.BOOKING_ALREADY_EXPIRED);
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new CinebookException(ErrorCode.PAYMENT_NOT_ALLOWED,
                    "Booking is in status " + booking.getStatus() + ", could not create payment");
        }

        List<Payment> existing = paymentRepository.findAllByBookingIdOrderByCreatedAtDesc(bookingId);
        boolean hasSuccess = existing.stream().anyMatch(p -> p.getStatus() == PaymentStatus.SUCCESS);
        if (hasSuccess) {
            throw new CinebookException(ErrorCode.PAYMENT_ALREADY_SUCCESS);
        }

        // amount ALWAYS GET from Booking, NEVER TRUST client
        int amount = booking.getTotalPrice();
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(amount)
                .paymentMethod(request.paymentMethod())
                .status(PaymentStatus.PENDING)
                .build();
        payment = paymentRepository.save(payment);

        var gatewayResult = paymentGateway.createPayment(payment.getId(), amount, booking.getId());
        payment.setProvider("MOCK");
        payment.setProviderTransactionId(gatewayResult.providerTransactionId());
        paymentRepository.save(payment);

        return paymentMapper.toResponse(payment, gatewayResult.paymentUrl());
    }

    // -----------------------------------------------------------
    // Callback
    // -----------------------------------------------------------
    @Transactional
    public void handleCallback(MockCallbackRequest request) {
        // Verify Signature BEFORE HIT DB
        var payload = new CallbackPayload(
                request.providerTransactionId(), request.amount(), request.status(),
                request.timestamp(), request.signature());

        if (!paymentGateway.verifySignature(payload)) {
            throw new CinebookException(ErrorCode.INVALID_PAYMENT_SIGNATURE);
        }

        Payment payment = paymentRepository.findByProviderTransactionId(request.providerTransactionId())
                .orElseThrow(() -> new CinebookException(ErrorCode.PAYMENT_NOT_FOUND));

        // Lock to handle callback - PESSIMISTIC
        payment = paymentRepository.findByIdForUpdate(payment.getId()).orElseThrow();

        // Idempotency: return OK if handle success, NO NEED to run business logic again
        if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED) {
            return;
        }

        // Verify amount from gateway match with Booking, NOT TRUST gateway
        Booking booking = payment.getBooking();
        if (request.amount() != booking.getTotalPrice()) {
            paymentStatusManager.changeStatus(payment, PaymentStatus.FAILED);
            throw new CinebookException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        boolean success = "SUCCESS".equals(request.status());
        PaymentStatus newStatus = success ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

        paymentStatusManager.changeStatus(payment, newStatus);

        if (success) {
            // Payment SUCCESS & Booking PAID must in 1 transaction
            bookingStatusManager.changeStatus(booking, BookingStatus.PAID);

            // ONLY release Redis lock + broadcast WebSocket AFTER transaction commit success,
            // NOT before/between - if commit failure (rollback), chair still valid.
            scheduleReleaseAfterCommit(booking);
        }
        // FAILED: Booking still PENDING, allow user create new Payment (new attempt) or
        // let Redis TTL + Scheduler auto cancel if overdue.
    }

    private void scheduleReleaseAfterCommit(Booking booking) {
        List<UUID> seatIds = bookingSeatRepository.findAllByBookingId(booking.getId()).stream()
                .map(bs -> bs.getSeat().getId()).toList();
        UUID showtimeId = booking.getShowtime().getId();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                seatLockService.forceUnlockSeats(showtimeId, seatIds);
                // NOTE: "released" - chair is no more TEMPORARY LOCKED - status SOLD.
            }
        });
    }

    // -----------------------------------------------------------
    // MOCK SUCCESS / MOCK FAILED BUTTON for FE
    // -----------------------------------------------------------
    @Transactional
    public void mockTrigger(UUID paymentId, boolean success) {
        Payment payment = findOrThrow(paymentId);

        long timestamp = Instant.now().getEpochSecond();
        String status = success ? "SUCCESS" : "FAILED";
        String signature = ((MockPaymentGateway) paymentGateway)
                .sign(payment.getProviderTransactionId(), payment.getAmount(), status, timestamp);

        handleCallback(new MockCallbackRequest(
                payment.getProviderTransactionId(), payment.getAmount(), status, timestamp, signature)
        );

    }

    // -----------------------------------------------------------
    // HELPERS
    // -----------------------------------------------------------
    public Payment findOrThrow(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new CinebookException(ErrorCode.PAYMENT_NOT_FOUND));
    }
}
