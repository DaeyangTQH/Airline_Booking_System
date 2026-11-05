package com.group3.infrastructure.persistence.entity;

import com.group3.domain.aggregate.PaymentMethod;
import com.group3.domain.aggregate.PaymentProvider;
import com.group3.domain.aggregate.PaymentStatus;
import com.group3.domain.vo.CurrencyCode;
import com.group3.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux_payments_idempotency_key",
                        columnNames = "idempotency_key"
                )
        },
        indexes = {
                @Index(name = "ix_payments_booking_id", columnList = "booking_id"),
                @Index(name = "ix_payments_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentEntity extends BaseEntity {

    @Id
    UUID id;

    @Column(name = "booking_id", nullable = false)
    UUID bookingId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    CurrencyCode currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 30)
    PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    PaymentProvider provider;

    @Column(name = "provider_txn_id", length = 100)
    String providerTxnId;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    String idempotencyKey;
}
