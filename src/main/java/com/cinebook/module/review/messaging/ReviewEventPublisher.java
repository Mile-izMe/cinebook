package com.cinebook.module.review.messaging;

import com.cinebook.module.review.event.MovieReviewedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.review.routing-key}")
    private String reviewRoutingKey;

    public void publishMovieReviewed(MovieReviewedEvent event) {
        rabbitTemplate.convertAndSend(exchange, reviewRoutingKey, event);
    }
}
