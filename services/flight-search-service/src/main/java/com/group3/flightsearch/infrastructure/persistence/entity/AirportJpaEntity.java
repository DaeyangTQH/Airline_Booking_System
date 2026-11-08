package com.group3.flightsearch.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "airports", uniqueConstraints =
        @UniqueConstraint(name = "uk_airports_code", columnNames = "code"))
@Getter
@Setter
@NoArgsConstructor
public class AirportJpaEntity {
    @Id
    private UUID id;

    @Column(nullable = false, length = 3, updatable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;
}
