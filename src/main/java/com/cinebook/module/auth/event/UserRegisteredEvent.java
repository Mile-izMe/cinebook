package com.cinebook.module.auth.event;

import java.io.Serializable;
import java.util.UUID;

/**
 * Published right after a user row is committed. Consumed asynchronously by
 * MailConsumer -> never send email synchronously inside the register request.
 */
public record UserRegisteredEvent(
        UUID userId,
        String email,
        String userName,
        String verifyToken
) implements Serializable {
}