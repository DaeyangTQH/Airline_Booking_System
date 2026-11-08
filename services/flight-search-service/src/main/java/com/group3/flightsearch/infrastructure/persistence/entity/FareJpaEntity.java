package com.group3.flightsearch.infrastructure.persistence.entity;

import com.group3.flightsearch.domain.model.FareClass;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@org.hibernate.annotations.Check(constraints = "base_price >= 0 AND valid_from < valid_to")
@Table(name = "fares", indexes = @Index(name = "idx_fares_flight_class", columnList = "flight_id,fare_class"))
@Getter
@Setter
@NoArgsConstructor
public class FareJpaEntity {
    @Id
    private UUID id;
    @Column(name = "flight_id", nullable = false)
    private UUID flightId;
    @Enumerated(EnumType.STRING)
    @Column(name = "fare_class", nullable = false)
    private FareClass fareClass;
    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal basePrice;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;
    @Column(name = "valid_to", nullable = false)
    private Instant validTo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flight_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_fares_flight"))
    private FlightJpaEntity flight;
}
