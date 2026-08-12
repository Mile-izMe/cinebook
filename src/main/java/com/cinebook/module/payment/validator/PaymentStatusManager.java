package com.cinebook.module.payment.validator;

import com.cinebook.module.payment.entity.Payment;
import com.cinebook.module.payment.entity.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class PaymentStatusManager {

    private final PaymentStatusTransitionPolicy paymentPolicy;

    public void changeStatus(
            Payment payment,
            PaymentStatus newStatus
    ) {

        paymentPolicy.validate(
                payment.getStatus(),
                newStatus
        );

        payment.changeStatus(newStatus);
        if (newStatus == PaymentStatus.SUCCESS) {
            payment.setPaidAt(Instant.now());
        }
    }
}
