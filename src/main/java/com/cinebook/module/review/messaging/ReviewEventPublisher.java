package com.cinebook.module.review.messaging;

import com.cinebook.module.review.event.MovieReviewedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReviewEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.review.routing-key}")
    private String reviewRoutingKey;

    public void publishMovieReviewed(MovieReviewedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMovieReviewedEventAfterCommit(MovieReviewedEvent event) {
        rabbitTemplate.convertAndSend(exchange, reviewRoutingKey, event);
    }
}
