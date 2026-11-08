package com.group3.flightsearch.infrastructure.persistence.repository;

import com.group3.flightsearch.infrastructure.persistence.entity.FareJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface FareJpaRepository extends JpaRepository<FareJpaEntity, UUID> {
    List<FareJpaEntity> findAllByFlightIdOrderByFareClassAscValidFromAsc(UUID flightId);
    List<FareJpaEntity> findAllByFlightIdInOrderByFareClassAscValidFromAsc(Collection<UUID> flightIds);
}
