package com.group3.domain.aggregate;

import com.group3.domain.vo.*;
import java.time.Instant;

public class Notification {
    private final NotificationId id;
    private final Recipient recipient;
    private final NotificationType type;
    private final Payload payload;
    private NotificationStatus status;
    private Instant sentAt;

    public Notification(NotificationId id, Recipient recipient, NotificationType type, Payload payload, NotificationStatus status, Instant sentAt) {
        this.id = id; this.recipient = recipient; this.type = type; this.payload = payload; this.status = status; this.sentAt = sentAt;
    }

    public static Notification create(Recipient recipient, NotificationType type, Payload payload) {
        return new Notification(new NotificationId(), recipient, type, payload, NotificationStatus.PENDING, null);
    }

    public void markAsSent() {
        if (this.status == NotificationStatus.SENT) throw new IllegalStateException("Đã gửi rồi");
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
    }

    public void markAsFailed() { this.status = NotificationStatus.FAILED; }

    // Getters
    public NotificationId getId() { return id; }
    public Recipient getRecipient() { return recipient; }
    public NotificationType getType() { return type; }
    public Payload getPayload() { return payload; }
    public NotificationStatus getStatus() { return status; }
    public Instant getSentAt() { return sentAt; }
}