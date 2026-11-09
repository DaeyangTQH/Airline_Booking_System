
package com.group3.infrastructure.adapter;

import com.group3.application.service.NotificationAppService;
import com.group3.domain.aggregate.Notification;
import com.group3.domain.vo.NotificationType;
import com.group3.domain.vo.Payload;
import com.group3.domain.vo.Recipient;
import com.group3.infrastructure.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
public class NotificationConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationAppService notificationAppService;
    private final JsonMapper jsonMapper;

    public NotificationConsumer(
            NotificationAppService notificationAppService,
            JsonMapper jsonMapper
    ) {
        this.notificationAppService = notificationAppService;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void receiveMessage(String rawMessage) {
        try {
            String email;
            String type;
            String content;

            if (rawMessage == null || rawMessage.isBlank()) {
                throw new IllegalArgumentException("Empty message");
            }

            if (rawMessage.trim().startsWith("{")) {
                // Booking Service sends JSON
                JsonNode event = jsonMapper.readTree(rawMessage);

                email = event.path("recipientEmail").asText("");
                type = event.path("type").asText("");
                content = event.path("message").asText("");
            } else {
                // User Service sends email|type|message
                String[] parts = rawMessage.split("\\|", 3);

                if (parts.length != 3) {
                    throw new IllegalArgumentException(
                            "Invalid notification message format"
                    );
                }

                email = parts[0];
                type = parts[1];
                content = parts[2];
            }

            Notification notification = Notification.create(
                    new Recipient(email),
                    NotificationType.valueOf(type),
                    new Payload(content)
            );

            notificationAppService.handleNotification(notification);

            log.info("Notification processed successfully, type={}", type);

        } catch (Exception e) {
            log.error("Failed to process RabbitMQ notification", e);

            // Do not silently acknowledge failed messages
            throw new IllegalStateException(
                    "Notification processing failed", e
            );
        }
    }
}
