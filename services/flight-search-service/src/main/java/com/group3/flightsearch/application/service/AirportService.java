package com.group3.flightsearch.application.service;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.flightsearch.application.command.CreateAirportCommand;
import com.group3.flightsearch.application.command.UpdateAirportCommand;
import com.group3.flightsearch.application.port.in.AirportUseCase;
import com.group3.flightsearch.application.query.AirportView;
import com.group3.flightsearch.domain.aggregate.Airport;
import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import com.group3.flightsearch.domain.repository.AirportRepository;
import com.group3.flightsearch.domain.valueobject.AirportCode;
import com.group3.flightsearch.domain.valueobject.AirportId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AirportService implements AirportUseCase {
    private final AirportRepository repository;

    @Override
    @Transactional
    public AirportView create(CreateAirportCommand command) {
        if (command == null) {
            throw new InvalidDomainValueException("Create airport command is required");
        }
        AirportCode code = new AirportCode(command.code());
        if (repository.existsByCode(code)) {
            throw new CustomException("AIRPORT_CODE_ALREADY_EXISTS", ErrorType.CONFLICT,
                    "Airport code already exists: " + code.value());
        }
        Airport airport = Airport.register(new AirportId(UUID.randomUUID()), code,
                command.name(), command.city(), command.country());
        return AirportView.from(repository.save(airport));
    }

    @Override
    @Transactional
    public AirportView update(UpdateAirportCommand command) {
        if (command == null) {
            throw new InvalidDomainValueException("Update airport command is required");
        }
        Airport airport = load(command.id());
        airport.rename(command.name());
        airport.changeLocation(command.city(), command.country());
        return AirportView.from(repository.save(airport));
    }

    @Override
    public AirportView getById(UUID id) {
        return AirportView.from(load(id));
    }

    @Override
    public List<AirportView> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidDomainValueException("Page must be >= 0 and size must be between 1 and 100");
        }
        return repository.findAll(page, size).stream().map(AirportView::from).toList();
    }

    private Airport load(UUID id) {
        return repository.findById(new AirportId(id))
                .orElseThrow(() -> new CustomException("AIRPORT_NOT_FOUND", ErrorType.NOT_FOUND,
                        "Airport not found: " + id));
    }
}
