package com.group3.infrastructure.adapter;

import com.group3.application.service.NotificationAppService;
import com.group3.domain.aggregate.Notification;
import com.group3.domain.vo.NotificationType;
import com.group3.domain.vo.Payload;
import com.group3.domain.vo.Recipient;
import com.group3.infrastructure.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final NotificationAppService notificationAppService;

    public NotificationConsumer(NotificationAppService notificationAppService) {
        this.notificationAppService = notificationAppService;
    }

    // Lắng nghe tin nhắn từ Queue
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void receiveMessage(String messageJson) {
        System.out.println(">>> Nhận được tin nhắn từ RabbitMQ: " + messageJson);

        try {
            // Parse chuỗi đơn giản: "email|type|message"
            String[] parts = messageJson.split("\\|");
            if (parts.length == 3) {
                Notification notification = Notification.create(
                        new Recipient(parts[0]),
                        NotificationType.valueOf(parts[1]),
                        new Payload(parts[2])
                );
                // Gọi lại hàm xử lý gửi mail đã viết trước đó
                notificationAppService.handleNotification(notification);
            } else {
                System.out.println(">>> Định dạng tin nhắn không hợp lệ!");
            }
        } catch (Exception e) {
            System.err.println(">>> Lỗi xử lý tin nhắn: " + e.getMessage());
        }
    }
}