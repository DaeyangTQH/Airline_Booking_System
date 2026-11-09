package com.group3.flightsearch.infrastructure.persistence.repository;

import com.group3.flightsearch.infrastructure.persistence.entity.AirportJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AirportJpaRepository extends JpaRepository<AirportJpaEntity, UUID> {
}
