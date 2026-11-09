package com.group3.flightsearch.application.service;

import com.group3.flightsearch.application.port.in.AirportUseCase;
import com.group3.flightsearch.application.query.AirportView;
import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import com.group3.flightsearch.domain.repository.AirportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AirportService implements AirportUseCase {
    private final AirportRepository repository;

    @Override
    public List<AirportView> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidDomainValueException("Page must be >= 0 and size must be between 1 and 100");
        }
        List<AirportView> result = new ArrayList<>();
        for (var airport : repository.findAll(page, size)) {
            result.add(AirportView.from(airport));
        }
        return List.copyOf(result);
    }

}
