package com.cinebook.module.payment.gateway;

import com.cinebook.module.payment.dto.response.CallbackPayload;
import com.cinebook.module.payment.dto.response.PaymentResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Fake gateway, but with a REAL HMAC signature scheme,
 * so signature verification can be demonstrated meaningfully instead of being an empty no-op.
 * <p>
 * Swap this for VNPAY, Momo later
 * without touching PaymentService - that's the point of the interface.
 */
@Component
public class MockPaymentGateway implements PaymentGateway {

    @Value("${app.payment.mock.secret}")
    private String secret;

    @Override
    public PaymentResult createPayment(UUID paymentId, int amount, String bookingCode) {
        String providerTransactionId = "MOCK-" + UUID.randomUUID();
        String paymentUrl = "/mock-payment?paymentId=" + paymentId;
        return new PaymentResult(paymentUrl, providerTransactionId);
    }

    @Override
    public boolean verifySignature(CallbackPayload payload) {
        String expected = sign(payload.providerTransactionId(), payload.amount(), payload.status(), payload.timestamp());
        return expected.equals(payload.signature());
    }

    public String sign(String providerTransactionId, int amount, String status, long timestamp) {
        try {
            String canonical = providerTransactionId + "|" + amount + "|" + status + "|" + timestamp;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign payload", e);
        }
    }

}
