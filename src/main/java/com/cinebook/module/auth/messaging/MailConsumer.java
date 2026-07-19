package com.cinebook.module.auth.messaging;

import com.cinebook.module.auth.event.UserRegisteredEvent;
import com.cinebook.module.auth.service.MailService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

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
    public void onUserRegistered(
            UserRegisteredEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("Consuming UserRegisteredEvent for email: {}", event.email());

        try {
            mailService.sendVerifyEmail(event.email(), event.userName(), event.verifyToken());

            // Confirm SUCCESS to RabbitMQ
            // Arg 1: ID of message
            // Arg 2: multiple = false (Only for this message)
            channel.basicAck(deliveryTag, false);

            log.info("Sent email and Ack successfully for: {}", event.email());
        } catch (Exception e) {
            log.error("Error sending email to {}. Reason: {}", event.email(), e.getMessage());

            // Confirm FAILURE for RabbitMQ to activate DLQ
            // Arg 1: ID of message
            // Arg 2: multiple = false
            // Arg 3: requeue = false (Notify RabbitMQ NOT to put in old queue)
            channel.basicNack(deliveryTag, false, false);

            log.warn("Message of {} denied and pushed to Dead Letter Queue (DLQ).", event.email());
        }
    }
}
