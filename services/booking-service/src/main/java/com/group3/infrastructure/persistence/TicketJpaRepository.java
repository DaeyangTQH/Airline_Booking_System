package com.group3.infrastructure.persistence;

import com.group3.infrastructure.persistence.entity.TicketEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketJpaRepository extends JpaRepository<TicketEntity, UUID> {

    Optional<TicketEntity> findByTicketNumber(String ticketNumber);

    Optional<TicketEntity> findByPassenger_Id(UUID passengerId);

    boolean existsByTicketNumber(String ticketNumber);

    boolean existsByPassenger_Id(UUID passengerId);

    @Query("""
        SELECT t FROM TicketEntity t
        JOIN FETCH t.passenger p
        JOIN FETCH p.booking b
        WHERE t.id = :ticketId AND b.userId = :userId
        """)
    Optional<TicketEntity> findDetailByIdAndUserId(
            @Param("ticketId") UUID ticketId,
            @Param("userId") UUID userId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t
        FROM TicketEntity t
        WHERE t.id = :ticketId
        """)
    Optional<TicketEntity> findByIdForUpdate(
            @Param("ticketId")
            UUID ticketId
    );
}
