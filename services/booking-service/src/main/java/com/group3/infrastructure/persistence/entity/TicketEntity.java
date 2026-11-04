package com.group3.infrastructure.persistence.entity;

import com.group3.domain.aggregate.TicketStatus;
import com.group3.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "tickets",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tickets_ticket_number",
                        columnNames = "ticket_number"
                ),
                @UniqueConstraint(
                        name = "uk_tickets_booking_passenger",
                        columnNames = "booking_passenger_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
public class TicketEntity extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "ticket_number", nullable = false)
    private String ticketNumber;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_passenger_id", nullable = false)
    private BookingPassengerEntity passenger;

    @Column(name = "seat_id")
    private UUID seatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TicketStatus status;
}
