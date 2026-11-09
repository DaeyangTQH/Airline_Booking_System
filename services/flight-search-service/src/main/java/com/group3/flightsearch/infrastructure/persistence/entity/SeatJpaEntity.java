package com.group3.flightsearch.infrastructure.persistence.entity;

import com.group3.flightsearch.domain.model.FareClass;
import com.group3.flightsearch.domain.model.SeatStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seats",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_seats_flight_number", columnNames = {"flight_id", "seat_number"}),
                @UniqueConstraint(name = "uk_seats_assigned_ticket", columnNames = "assigned_ticket_id")},
        indexes = @Index(name = "idx_seats_flight_class_status", columnList = "flight_id,seat_class,status"))
@org.hibernate.annotations.Check(constraints =
        "((status = 'HELD' AND held_by_booking_id IS NOT NULL AND hold_expires_at IS NOT NULL) OR "
        + "(status <> 'HELD' AND held_by_booking_id IS NULL AND hold_expires_at IS NULL)) "
        + "AND (assigned_ticket_id IS NULL OR status = 'BOOKED')")
@Getter
@Setter
@NoArgsConstructor
public class SeatJpaEntity {
    @Id
    private UUID id;
    @Column(name = "flight_id", nullable = false)
    private UUID flightId;
    @Column(name = "seat_number", nullable = false)
    private String seatNumber;
    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false)
    private FareClass seatClass;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;
    // External Booking reference only; no cross-service FK or JPA relationship.
    @Column(name = "held_by_booking_id")
    private UUID heldByBookingId;
    @Column(name = "hold_expires_at")
    private Instant holdExpiresAt;
    // A ticket can own at most one seat, including across different flights.
    @Column(name = "assigned_ticket_id")
    private UUID assignedTicketId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flight_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_seats_flight"))
    private FlightJpaEntity flight;
}
