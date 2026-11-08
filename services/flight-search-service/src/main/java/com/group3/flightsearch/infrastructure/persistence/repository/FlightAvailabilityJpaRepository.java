package com.group3.flightsearch.infrastructure.persistence.repository;

import com.group3.flightsearch.infrastructure.persistence.entity.FlightAvailabilityId;
import com.group3.flightsearch.infrastructure.persistence.entity.FlightAvailabilityJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface FlightAvailabilityJpaRepository
        extends JpaRepository<FlightAvailabilityJpaEntity, FlightAvailabilityId> {
    List<FlightAvailabilityJpaEntity> findAllByFlightIdOrderByFareClassAsc(UUID flightId);
    List<FlightAvailabilityJpaEntity> findAllByFlightIdInOrderByFareClassAsc(Collection<UUID> flightIds);

    @Modifying(flushAutomatically = true)
    @Query(value = """
            INSERT INTO flight_availability (flight_id, fare_class, available_seats, class_capacity)
            SELECT flight.id, classes.fare_class,
                CAST(COUNT(seat.id) FILTER (WHERE seat.status = 'AVAILABLE') AS integer),
                CAST(COUNT(seat.id) AS integer)
            FROM flights flight
            CROSS JOIN (VALUES ('ECONOMY'), ('BUSINESS'), ('FIRST')) AS classes(fare_class)
            LEFT JOIN seats seat ON seat.flight_id = flight.id AND seat.seat_class = classes.fare_class
            WHERE flight.id = :flightId
            GROUP BY flight.id, classes.fare_class
            ON CONFLICT (flight_id, fare_class) DO UPDATE
            SET available_seats = EXCLUDED.available_seats, class_capacity = EXCLUDED.class_capacity
            """, nativeQuery = true)
    void refresh(@Param("flightId") UUID flightId);
}
