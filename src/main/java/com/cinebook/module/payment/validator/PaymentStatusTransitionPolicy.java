package com.cinebook.module.payment.validator;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.module.payment.entity.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class PaymentStatusTransitionPolicy {

    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED = Map.of(
            PaymentStatus.PENDING, Set.of(PaymentStatus.SUCCESS, PaymentStatus.FAILED, PaymentStatus.CANCELLED),
            PaymentStatus.SUCCESS, Set.of(PaymentStatus.REFUNDED),
            PaymentStatus.FAILED, Set.of(),
            PaymentStatus.CANCELLED, Set.of(),
            PaymentStatus.REFUNDED, Set.of()
    );

    public void validate(PaymentStatus from, PaymentStatus to) {
        if (!ALLOWED.getOrDefault(from, Set.of()).contains(to)) {
            throw new CinebookException(ErrorCode.INVALID_PAYMENT_TRANSITION,
                    "Can not convert from status %s to %s".formatted(from, to));
        }
    }
}
