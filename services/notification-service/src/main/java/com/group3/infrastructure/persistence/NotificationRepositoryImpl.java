package com.group3.infrastructure.persistence;
import com.group3.domain.aggregate.Notification;
import com.group3.domain.repository.NotificationRepository;
import com.group3.domain.vo.*;
import com.group3.infrastructure.persistence.entity.NotificationJpaEntity;
import com.group3.infrastructure.persistence.jpa.JpaNotificationRepository;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepositoryImpl implements NotificationRepository {
    private final JpaNotificationRepository jpa;
    public NotificationRepositoryImpl(JpaNotificationRepository jpa) { this.jpa = jpa; }

    @Override public Notification save(Notification n) { return toDomain(jpa.save(toEntity(n))); }
    @Override public Notification findById(NotificationId id) { return jpa.findById(id.value()).map(this::toDomain).orElse(null); }

    private NotificationJpaEntity toEntity(Notification d) {
        NotificationJpaEntity e = new NotificationJpaEntity();
        e.setId(d.getId().value());
        e.setRecipient(d.getRecipient().value());
        e.setType(d.getType().name());
        String jsonPayload = String.format("{\"content\": \"%s\"}", d.getPayload().content().replace("\"", "\\\""));
        e.setPayload(jsonPayload);

        e.setStatus(d.getStatus().name());
        e.setSentAt(d.getSentAt());
        return e;
    }
    private Notification toDomain(NotificationJpaEntity e) {
        String rawContent = e.getPayload();
        if (rawContent != null && rawContent.startsWith("{\"content\": \"")) {
            rawContent = rawContent.substring(13, rawContent.length() - 2); // Cắt chuỗi
        }

        return new Notification(
                new NotificationId(e.getId()),
                new Recipient(e.getRecipient()),
                NotificationType.valueOf(e.getType()),
                new Payload(rawContent != null ? rawContent : ""),
                NotificationStatus.valueOf(e.getStatus()),
                e.getSentAt()
        );
    }
}