package com.group3.infrastructure.persistence.entity;

import com.group3.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "outbox_events",
        indexes = {
                @Index(
                        name = "idx_outbox_status_created_at",
                        columnList = "status, created_at"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEventEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    public static OutboxEventEntity pending(
            UUID eventId,
            String eventType,
            String payload
    ) {
        if (eventId == null) {
            throw new IllegalArgumentException(
                    "Event ID must not be null"
            );
        }

        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException(
                    "Event type must not be blank"
            );
        }

        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException(
                    "Event payload must not be blank"
            );
        }

        OutboxEventEntity entity =
                new OutboxEventEntity();

        entity.id = eventId;
        entity.eventType = eventType;
        entity.payload = payload;
        entity.status = OutboxStatus.PENDING;
        entity.retryCount = 0;

        return entity;
    }

    public void markPublished() {
        status = OutboxStatus.PUBLISHED;
        publishedAt = Instant.now();
        lastError = null;
    }

    public void registerFailure(
            String errorMessage,
            int maxRetries
    ) {
        retryCount++;

        lastError = normalizeErrorMessage(
                errorMessage
        );

        if (retryCount >= maxRetries) {
            status = OutboxStatus.FAILED;
        }
    }

    private String normalizeErrorMessage(
            String errorMessage
    ) {
        if (errorMessage == null
                || errorMessage.isBlank()) {
            return "Unknown publishing error";
        }

        String normalized =
                errorMessage.trim();

        return normalized.length() <= 1000
                ? normalized
                : normalized.substring(0, 1000);
    }
}
