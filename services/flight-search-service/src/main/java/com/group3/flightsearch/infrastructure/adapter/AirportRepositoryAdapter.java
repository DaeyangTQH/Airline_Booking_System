package com.group3.flightsearch.infrastructure.adapter;

import com.group3.flightsearch.domain.aggregate.Airport;
import com.group3.flightsearch.domain.repository.AirportRepository;
import com.group3.flightsearch.infrastructure.persistence.mapper.AirportPersistenceMapper;
import com.group3.flightsearch.infrastructure.persistence.repository.AirportJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class AirportRepositoryAdapter implements AirportRepository {
    private final AirportJpaRepository repository;
    private final AirportPersistenceMapper mapper;

    @Override
    public List<Airport> findAll(int page, int size) {
        var rows = repository.findAll(PageRequest.of(page, size, Sort.by("code", "id")));
        List<Airport> result = new ArrayList<>();
        for (var row : rows) {
            result.add(mapper.toDomain(row));
        }
        return List.copyOf(result);
    }
}
