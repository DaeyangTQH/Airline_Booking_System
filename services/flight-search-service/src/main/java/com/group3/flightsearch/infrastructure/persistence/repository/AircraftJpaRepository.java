package com.group3.flightsearch.infrastructure.persistence.repository;

import com.group3.flightsearch.infrastructure.persistence.entity.AircraftJpaEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface AircraftJpaRepository extends JpaRepository<AircraftJpaEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select aircraft from AircraftJpaEntity aircraft where aircraft.id = :id")
    Optional<AircraftJpaEntity> findByIdForUpdate(@Param("id") UUID id);
}
