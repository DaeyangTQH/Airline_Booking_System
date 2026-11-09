package com.group3.flightsearch.infrastructure.persistence.repository;

import com.group3.flightsearch.domain.model.FareClass;
import com.group3.flightsearch.infrastructure.persistence.entity.SeatJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.UUID;
import java.util.Optional;

public interface SeatJpaRepository extends JpaRepository<SeatJpaEntity, UUID> {
    Optional<SeatJpaEntity> findByAssignedTicketId(UUID ticketId);
    boolean existsByFlightIdAndSeatNumber(UUID flightId, String seatNumber);
    long countByFlightId(UUID flightId);
    Page<SeatJpaEntity> findAllByFlightId(UUID flightId, Pageable pageable);
    Page<SeatJpaEntity> findAllByFlightIdAndSeatClass(UUID flightId, FareClass seatClass, Pageable pageable);

    @Query(value = """
            SELECT CAST(COALESCE(MAX(assigned.seat_count), 0) AS integer)
            FROM (
                SELECT COUNT(*) AS seat_count
                FROM seats seat JOIN flights flight ON flight.id = seat.flight_id
                WHERE flight.aircraft_id = :aircraftId
                GROUP BY flight.id
            ) assigned
            """, nativeQuery = true)
    int maxAssignedSeatsByAircraft(@Param("aircraftId") UUID aircraftId);
}
