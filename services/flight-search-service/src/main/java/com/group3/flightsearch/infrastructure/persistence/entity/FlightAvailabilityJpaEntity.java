package com.group3.flightsearch.infrastructure.persistence.entity;

import com.group3.flightsearch.domain.model.FareClass;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "flight_availability")
@IdClass(FlightAvailabilityId.class)
@org.hibernate.annotations.Check(constraints = "available_seats >= 0 AND class_capacity >= 0 AND available_seats <= class_capacity")
@Getter
@NoArgsConstructor
public class FlightAvailabilityJpaEntity {
    @Id
    @Column(name = "flight_id", nullable = false)
    private java.util.UUID flightId;
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "fare_class", nullable = false)
    private FareClass fareClass;
    @Column(name = "available_seats", nullable = false)
    private int availableSeats;
    @Column(name = "class_capacity", nullable = false)
    private int classCapacity;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flight_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_availability_flight"))
    private FlightJpaEntity flight;
}
