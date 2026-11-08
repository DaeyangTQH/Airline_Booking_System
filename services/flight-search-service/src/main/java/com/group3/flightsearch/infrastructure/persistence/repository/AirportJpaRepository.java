package com.group3.flightsearch.infrastructure.persistence.repository;

import com.group3.flightsearch.infrastructure.persistence.entity.AirportJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface AirportJpaRepository extends JpaRepository<AirportJpaEntity, UUID> {
    boolean existsByCode(String code);
    Optional<AirportJpaEntity> findByCode(String code);
}
