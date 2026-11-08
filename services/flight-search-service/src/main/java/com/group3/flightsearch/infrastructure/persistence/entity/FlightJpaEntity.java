package com.group3.flightsearch.infrastructure.persistence.entity;

import com.group3.flightsearch.domain.model.FlightStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@org.hibernate.annotations.Check(constraints = "origin_code <> destination_code AND arrival_time > departure_time")
@Table(name = "flights", indexes = @Index(name = "idx_flights_route_departure",
        columnList = "origin_code,destination_code,departure_time"))
@Getter
@Setter
@NoArgsConstructor
public class FlightJpaEntity {
    @Id
    private UUID id;
    @Column(name = "flight_code", nullable = false)
    private String flightCode;
    @Column(name = "origin_code", nullable = false, length = 3)
    private String originCode;
    @Column(name = "destination_code", nullable = false, length = 3)
    private String destinationCode;
    @Column(name = "departure_time", nullable = false)
    private Instant departureTime;
    @Column(name = "arrival_time", nullable = false)
    private Instant arrivalTime;
    @Column(name = "aircraft_id", nullable = false)
    private UUID aircraftId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "origin_code", referencedColumnName = "code", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_flights_origin"))
    private AirportJpaEntity origin;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_code", referencedColumnName = "code", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_flights_destination"))
    private AirportJpaEntity destination;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aircraft_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_flights_aircraft"))
    private AircraftJpaEntity aircraft;
}
