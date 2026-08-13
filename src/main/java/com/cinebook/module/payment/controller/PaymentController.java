package com.cinebook.module.payment.controller;

import com.cinebook.common.response.ApiSuccessResponse;
import com.cinebook.common.security.CustomerUserDetails;
import com.cinebook.module.payment.dto.request.CreatePaymentRequest;
import com.cinebook.module.payment.dto.request.MockCallbackRequest;
import com.cinebook.module.payment.dto.response.PaymentResponse;
import com.cinebook.module.payment.service.PaymentService;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/api/bookings/{bookingId}/payment")
    public ResponseEntity<ApiSuccessResponse<PaymentResponse>> create(
            @PathVariable UUID bookingId,
            @Nullable @AuthenticationPrincipal CustomerUserDetails userDetails,
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        UUID userId = (userDetails != null) ? userDetails.getUserId() : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.<PaymentResponse>builder()
                .message("Create payment success")
                .data(paymentService.createPayment(userId, bookingId, request))
                .build());
    }

    /**
     * Endpoint that a REAL gateway would call (unauthenticated - gateway
     * doesn't have our JWT). Security lives entirely in signature verification,
     * not in Spring Security auth.
     */
    @PostMapping("/api/payments/callback")
    public ResponseEntity<Void> callback(@RequestBody MockCallbackRequest request) {
        paymentService.handleCallback(request);
        return ResponseEntity.ok().build();
    }

    // MOCK SUCCESS / MOCK FAILED BUTTON, virtual real callback
    @PostMapping("/api/payments/{paymentId}/mock-success")
    public ResponseEntity<Void> mockSuccess(@PathVariable UUID paymentId) {
        paymentService.mockTrigger(paymentId, true);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/payments/{paymentId}/mock-failed")
    public ResponseEntity<Void> mockFailed(@PathVariable UUID paymentId) {
        paymentService.mockTrigger(paymentId, false);
        return ResponseEntity.ok().build();
    }
}
