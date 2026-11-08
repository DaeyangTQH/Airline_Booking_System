package com.group3.flightsearch.domain.repository;

import com.group3.flightsearch.domain.aggregate.Aircraft;
import com.group3.flightsearch.domain.valueobject.AircraftId;
import java.util.List;
import java.util.Optional;

public interface AircraftRepository {
    Aircraft save(Aircraft aircraft);
    Optional<Aircraft> findById(AircraftId id);
    Optional<Aircraft> findByIdForUpdate(AircraftId id);
    List<Aircraft> findAll(int page, int size);
}
