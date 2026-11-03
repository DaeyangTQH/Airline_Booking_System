package com.group3.infrastructure.persistence.entity;

import com.group3.domain.aggregate.BookingStatus;
import com.group3.domain.vo.CurrencyCode;
import com.group3.domain.vo.FareClass;
import com.group3.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "bookings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bookings_booking_ref",
                        columnNames = "booking_ref"
                )
        },
        indexes = {
                @Index(
                        name = "idx_bookings_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_bookings_flight_id",
                        columnList = "flight_id"
                ),
                @Index(
                        name = "idx_bookings_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_bookings_expires_at",
                        columnList = "expires_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class BookingEntity extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "booking_ref", nullable = false, length = 6)
    private String bookingReference;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "flight_id", nullable = false)
    private UUID flightId;

    @Enumerated(EnumType.STRING)
    @Column(name = "fare_class", nullable = false)
    private FareClass fareClass;

    @Column(name = "ticket_quantity", nullable = false)
    private Integer ticketQuantity;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private CurrencyCode currency;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name ="email", nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

}
