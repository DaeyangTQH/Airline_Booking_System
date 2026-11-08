package com.group3.flightsearch.domain.repository;

import com.group3.flightsearch.domain.aggregate.Airport;
import com.group3.flightsearch.domain.valueobject.AirportCode;
import com.group3.flightsearch.domain.valueobject.AirportId;
import java.util.List;
import java.util.Optional;

public interface AirportRepository {
    Airport save(Airport airport);
    Optional<Airport> findById(AirportId id);
    boolean existsByCode(AirportCode code);
    List<Airport> findAll(int page, int size);
}
