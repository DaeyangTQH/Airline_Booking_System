package com.group3.flightsearch.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "aircrafts")
@Getter
@Setter
@NoArgsConstructor
public class AircraftJpaEntity {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String model;

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;
}
