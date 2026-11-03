package com.group3.application.service;

import com.group3.application.port.EmailSenderPort;
import com.group3.domain.aggregate.Notification;
import com.group3.domain.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationAppService {
    private final NotificationRepository repository;
    private final EmailSenderPort emailSender;

    public NotificationAppService(NotificationRepository repository, EmailSenderPort emailSender) {
        this.repository = repository;
        this.emailSender = emailSender;
    }

    public void handleNotification(Notification notification) {
        repository.save(notification); // Lưu PENDING
        try {
            emailSender.send(notification.getRecipient(), notification.getType().name(), notification.getPayload().content());
            notification.markAsSent();
            repository.save(notification); // Cập nhật SENT
        } catch (Exception e) {
            notification.markAsFailed();
            repository.save(notification); // Cập nhật FAILED vào DB
            // Ném lỗi ra ngoài để GlobalExceptionHandler bắt và trả về Postman
            throw e;
        }
    }
}