package com.cinebook.module.payment.repository;

import com.cinebook.module.payment.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findAllByBookingIdOrderByCreatedAtDesc(UUID bookingId);

    /**
     * Locks the row for the duration of the transaction - PESSIMISTIC LOCK
     * Case: 3 concurrent callbacks all hit this - only the first to acquire the row
     * lock proceeds; the other two block here, then see status already SUCCESS
     * and short-circuit (return OK) instead of reprocessing.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Payment p WHERE p.id = :id")
    Optional<Payment> findByIdForUpdate(java.util.UUID id);

    Optional<Payment> findByProviderTransactionId(String providerTransactionId);
}
