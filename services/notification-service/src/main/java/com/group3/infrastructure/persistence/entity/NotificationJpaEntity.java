package com.group3.infrastructure.persistence.entity;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationJpaEntity {
    @Id private UUID id;
    private String recipient;
    private String type;
    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;
    private String status;
    private Instant sentAt;

    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public String getRecipient() { return recipient; } public void setRecipient(String recipient) { this.recipient = recipient; }
    public String getType() { return type; } public void setType(String type) { this.type = type; }
    public String getPayload() { return payload; } public void setPayload(String payload) { this.payload = payload; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Instant getSentAt() { return sentAt; } public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
}