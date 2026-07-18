package com.cinebook.module.auth.messaging;

import com.cinebook.module.auth.event.UserRegisteredEvent;
import com.cinebook.module.auth.service.MailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MailConsumer {

    private static final Logger log = LoggerFactory.getLogger(MailConsumer.class);

    private final MailService mailService;

    /**
     * Runs on a separate consumer thread, decoupled from the HTTP request
     * that triggered registration. If Mailpit/SMTP is briefly down, RabbitMQ
     * keeps the message queued instead of failing the user's request.
     */
    @RabbitListener(queues = "${rabbitmq.mail.queue}")
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Consuming UserRegisteredEvent for userId={}", event.userId());
        mailService.sendVerifyEmail(event.email(), event.userName(), event.verifyToken());
    }
}
