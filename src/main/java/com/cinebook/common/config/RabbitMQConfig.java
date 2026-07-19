package com.cinebook.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // --- Main network ---
    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.mail.queue}")
    private String mailQueueName;

    @Value("${rabbitmq.mail.routing-key}")
    private String mailRoutingKey;

    // --- Variable for Dead Letter Queue (DLQ) ---
    @Value("${rabbitmq.dlx}")
    private String dlxName;

    @Value("${rabbitmq.dlq.mail.queue}")
    private String mailDlqName;

    @Value("${rabbitmq.dlq.mail.routing-key}")
    private String mailDlqRoutingKey;

    // ==========================================
    // 1. CONFIG DEAD LETTER (DLX & DLQ)
    // ==========================================
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxName, true, false);
    }

    @Bean
    public Queue deadLetterMailQueue() {
        return new Queue(mailDlqName, true);
    }

    @Bean
    public Binding deadLetterMailBinding() {
        return BindingBuilder.bind(deadLetterMailQueue())
                .to(deadLetterExchange())
                .with(mailDlqRoutingKey);
    }

    // ==========================================
    // 2. CONFIG MAIN NETWORK
    // ==========================================
    @Bean
    public DirectExchange cinebookExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue mailQueue() {
        // durable queue so messages survive a broker restart
        return QueueBuilder.durable(mailQueueName)
                .withArgument("x-dead-letter-exchange", dlxName)
                .withArgument("x-dead-letter-routing-key", mailDlqRoutingKey)
                .build();
    }

    @Bean
    public Binding mailBinding(Queue mailQueue, DirectExchange cinebookExchange) {
        return BindingBuilder.bind(mailQueue).to(cinebookExchange).with(mailRoutingKey);
    }

    // ==========================================
    // 3. CONFIG CONVERTER & LISTENER
    // ==========================================
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        // --- PERFORMANCE OPTIMIZATION CONFIGURATIONS FOR THE FUTURE ---

        // Number of concurrent consumers running in parallel to process messages (Thread pool)
        // factory.setConcurrentConsumers(2);
        // factory.setMaxConcurrentConsumers(5);

        // Prefetch count: The maximum number of messages a Worker fetches at once.
        // Setting this to 1 is highly beneficial for time-consuming tasks (like audio processing),
        // helping to evenly distribute the load across different instances if you scale the app.
        // factory.setPrefetchCount(1);

        return factory;
    }

}
