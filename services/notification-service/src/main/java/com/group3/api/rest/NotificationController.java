package com.group3.api.rest;
import com.group3.api.dto.NotificationRequest;
import com.group3.application.service.NotificationAppService;
import com.group3.domain.aggregate.Notification;
import com.group3.domain.vo.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationAppService service;
    public NotificationController(NotificationAppService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<?> send(@RequestBody NotificationRequest req) {
        Notification n = Notification.create(
                new Recipient(req.email()),
                NotificationType.valueOf(req.type()),
                new Payload(req.message())
        );
        service.handleNotification(n);
        return ResponseEntity.ok("Đã xử lý thông báo");
    }
}