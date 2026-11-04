package com.group3.infrastructure.persistence.entity;

import com.group3.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "outbox_events",
        indexes = {
                @Index(
                        name = "ix_outbox_unpublished",
                        columnList = "published, created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OutboxEventEntity extends BaseEntity {

    @Id
    UUID id;

    @Column(name = "aggregate_id", nullable = false)
    UUID aggregateId;

    @Column(name = "type", nullable = false, length = 100)
    String type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    String payload;

    @Column(name = "published", nullable = false)
    boolean published;

    @Column(name = "published_at")
    Instant publishedAt;
}
