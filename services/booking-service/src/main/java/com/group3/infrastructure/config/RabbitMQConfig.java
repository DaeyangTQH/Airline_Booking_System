package com.group3.infrastructure.config;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue notificationQueue(
            @Value("${app.messaging.notification-queue}")
            String queueName
    ) {
        return new Queue(
                queueName,
                true
        );
    }
}