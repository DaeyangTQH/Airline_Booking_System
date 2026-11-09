package com.group3.flightsearch.infrastructure.persistence.repository;

import com.group3.flightsearch.domain.model.FareClass;
import com.group3.flightsearch.infrastructure.persistence.entity.SeatJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface SeatJpaRepository extends JpaRepository<SeatJpaEntity, UUID> {
    Optional<SeatJpaEntity> findByAssignedTicketId(UUID ticketId);
    Page<SeatJpaEntity> findAllByFlightId(UUID flightId, Pageable pageable);
    Page<SeatJpaEntity> findAllByFlightIdAndSeatClass(UUID flightId, FareClass seatClass, Pageable pageable);

}
