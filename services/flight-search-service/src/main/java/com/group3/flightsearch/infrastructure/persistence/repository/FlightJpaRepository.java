package com.group3.flightsearch.infrastructure.persistence.repository;

import com.group3.flightsearch.infrastructure.persistence.entity.FlightJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.Optional;
import java.util.UUID;

public interface FlightJpaRepository extends JpaRepository<FlightJpaEntity, UUID>, JpaSpecificationExecutor<FlightJpaEntity> {
    @Override
    @EntityGraph(attributePaths = {"origin", "destination", "aircraft"})
    Optional<FlightJpaEntity> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = {"origin", "destination", "aircraft"})
    Page<FlightJpaEntity> findAll(Specification<FlightJpaEntity> specification, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select flight from FlightJpaEntity flight where flight.id = :id")
    Optional<FlightJpaEntity> findByIdForUpdate(@Param("id") UUID id);
}
