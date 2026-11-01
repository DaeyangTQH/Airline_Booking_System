package com.group3.infrastructure.persistence;

import com.group3.infrastructure.persistence.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketJpaRepository extends JpaRepository<TicketEntity, UUID> {

    Optional<TicketEntity> findByTicketNumber(String ticketNumber);

    Optional<TicketEntity> findByPassenger_Id(UUID passengerId);

    boolean existsByTicketNumber(String ticketNumber);

    boolean existsByPassenger_Id(UUID passengerId);
}
