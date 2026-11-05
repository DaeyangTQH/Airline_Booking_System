package com.group3.infrastructure.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Tên Hàng đợi (Queue) mà Booking Service sẽ gửi tin nhắn đến
    public static final String NOTIFICATION_QUEUE = "notification_queue";

    @Bean
    public Queue notificationQueue() {
        // durable = true: tin nhắn không bị mất khi RabbitMQ restart
        return new Queue(NOTIFICATION_QUEUE, true);
    }
}