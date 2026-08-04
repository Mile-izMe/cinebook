package com.cinebook.module.booking.validator;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.module.booking.entity.BookingStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Single source of truth for which BookingStatus transitions are legal.
 * No other class should mutate Booking.status directly without going
 * through validate() first.
 */
@Component
public class BookingStatusTransitionPolicy {

    private static final Map<BookingStatus, Set<BookingStatus>> ALLOWED = Map.of(
            BookingStatus.PENDING, Set.of(BookingStatus.CANCELLED, BookingStatus.EXPIRED, BookingStatus.PAID),
            BookingStatus.PAID, Set.of(BookingStatus.USED),
            BookingStatus.CANCELLED, Set.of(),
            BookingStatus.EXPIRED, Set.of(),
            BookingStatus.USED, Set.of()
    );

    public void validate(BookingStatus from, BookingStatus to) {
        if (!ALLOWED.getOrDefault(from, Set.of()).contains(to)) {
            throw new CinebookException(ErrorCode.INVALID_BOOKING_TRANSITION,
                    "Can not convert from status %s to %s".formatted(from, to));
        }
    }
}
