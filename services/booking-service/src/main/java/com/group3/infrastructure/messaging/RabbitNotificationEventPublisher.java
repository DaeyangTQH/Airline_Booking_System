package com.group3.infrastructure.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitNotificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String notificationQueue;

    public RabbitNotificationEventPublisher(
            RabbitTemplate rabbitTemplate,

            @Value("${app.messaging.notification-queue}")
            String notificationQueue
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.notificationQueue = notificationQueue;
    }

    public void publish(
            String payload
    ) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException(
                    "Notification payload must not be blank"
            );
        }

        rabbitTemplate.convertAndSend(
                notificationQueue,
                payload
        );
    }
}
