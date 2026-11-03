package com.group3.infrastructure.persistence.entity;

import com.group3.domain.aggregate.RefundStatus;
import com.group3.domain.vo.CurrencyCode;
import com.group3.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
        name = "refunds",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux_refunds_idempotency_key",
                        columnNames = "idempotency_key"
                )
        },
        indexes = {
                @Index(name = "ix_refunds_payment_id", columnList = "payment_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefundEntity extends BaseEntity {

    @Id
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    PaymentEntity payment;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 3)
    CurrencyCode currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    RefundStatus status;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    String idempotencyKey;

    @Column(name = "provider_refund_id", length = 100)
    String providerRefundId;
}
