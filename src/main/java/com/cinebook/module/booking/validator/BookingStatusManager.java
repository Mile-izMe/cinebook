package com.cinebook.module.booking.validator;

import com.cinebook.module.booking.entity.Booking;
import com.cinebook.module.booking.entity.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingStatusManager {

    private final BookingStatusTransitionPolicy bookingPolicy;

    public void changeStatus(
            Booking booking,
            BookingStatus newStatus
    ) {

        bookingPolicy.validate(
                booking.getStatus(),
                newStatus
        );

        booking.changeStatus(newStatus);
    }
}
