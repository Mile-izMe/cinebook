package com.cinebook.module.booking.entity;

import com.cinebook.common.entity.Auditable;
import com.cinebook.module.showtime.entity.Showtime;
import com.cinebook.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "guest_phone")
    private String guestPhone;

    @Column(name = "booking_code", unique = true, length = 10)
    private String bookingCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot", columnDefinition = "jsonb")
    private BookingSnapshot snapshot;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "booking_time", nullable = false)
    private Instant bookingTime;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public void changeStatus(BookingStatus newStatus) {
        this.status = newStatus;
    }

    public boolean isGuestBooking() {
        return user == null;
    }
}
