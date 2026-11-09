package com.group3.flightsearch.domain.repository;

import com.group3.flightsearch.domain.aggregate.Airport;
import java.util.List;

public interface AirportRepository {
    List<Airport> findAll(int page, int size);
}
