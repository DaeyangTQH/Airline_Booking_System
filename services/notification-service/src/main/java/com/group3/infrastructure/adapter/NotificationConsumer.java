package com.group3.infrastructure.adapter;

import com.group3.api.dto.NotificationMessage;
import com.group3.application.service.NotificationAppService;
import com.group3.domain.aggregate.Notification;
import com.group3.domain.vo.NotificationType;
import com.group3.domain.vo.Payload;
import com.group3.domain.vo.Recipient;
import com.group3.infrastructure.config.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final NotificationAppService notificationAppService;
    private final ObjectMapper objectMapper;

    public NotificationConsumer(NotificationAppService notificationAppService, ObjectMapper objectMapper) {
        this.notificationAppService = notificationAppService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void receiveMessage(String messageJson) {
        System.out.println(">>> Nhận được tin nhắn từ RabbitMQ: " + messageJson);

        try {
            NotificationMessage msg;

            // KIỂM TRA ĐỊNH DẠNG: Nếu là JSON (bắt đầu bằng '{') thì parse JSON
            if (messageJson.trim().startsWith("{")) {
                msg = objectMapper.readValue(messageJson, NotificationMessage.class);
            } else {
                // Nếu là định dạng cũ (email|type|message) thì cắt chuỗi
                String[] parts = messageJson.split("\\|");
                if (parts.length == 3) {
                    msg = new NotificationMessage(parts[0], parts[1], parts[2]);
                } else {
                    System.err.println(">>> Định dạng tin nhắn không hợp lệ (không phải JSON hoặc email|type|message)!");
                    return;
                }
            }

            // Tạo Notification từ dữ liệu đã parse
            Notification notification = Notification.create(
                    new Recipient(msg.email()),
                    NotificationType.valueOf(msg.type()),
                    new Payload(msg.message())
            );

            // Gửi thông báo
            notificationAppService.handleNotification(notification);

        } catch (Exception e) {
            System.err.println(">>> Lỗi xử lý tin nhắn: " + e.getMessage());
            e.printStackTrace();
        }
    }
}