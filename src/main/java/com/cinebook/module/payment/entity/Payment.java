package com.cinebook.module.payment.entity;

import com.cinebook.common.entity.Auditable;
import com.cinebook.module.booking.entity.Booking;
import com.cinebook.module.booking.entity.BookingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "provider_transaction_id")
    private String providerTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private Instant paidAt;

    public void changeStatus(PaymentStatus newStatus) {
        this.status = newStatus;
    }

}
