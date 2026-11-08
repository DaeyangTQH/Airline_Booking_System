package com.group3.flightsearch.application.service;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.flightsearch.application.command.CreateAircraftCommand;
import com.group3.flightsearch.application.command.UpdateAircraftCommand;
import com.group3.flightsearch.application.port.in.AircraftUseCase;
import com.group3.flightsearch.application.query.AircraftView;
import com.group3.flightsearch.domain.aggregate.Aircraft;
import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import com.group3.flightsearch.domain.repository.AircraftRepository;
import com.group3.flightsearch.domain.repository.SeatRepository;
import com.group3.flightsearch.domain.valueobject.AircraftId;
import com.group3.flightsearch.domain.valueobject.AircraftModel;
import com.group3.flightsearch.domain.valueobject.SeatCapacity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AircraftService implements AircraftUseCase {
    private final AircraftRepository repository;
    private final SeatRepository seats;

    @Override
    @Transactional
    public AircraftView create(CreateAircraftCommand command) {
        if (command == null) {
            throw new InvalidDomainValueException("Create aircraft command is required");
        }
        Aircraft aircraft = Aircraft.register(new AircraftId(UUID.randomUUID()),
                new AircraftModel(command.model()), new SeatCapacity(command.totalSeats()));
        return AircraftView.from(repository.save(aircraft));
    }

    @Override
    @Transactional
    public AircraftView update(UpdateAircraftCommand command) {
        if (command == null) {
            throw new InvalidDomainValueException("Update aircraft command is required");
        }
        AircraftModel model = new AircraftModel(command.model());
        SeatCapacity capacity = new SeatCapacity(command.totalSeats());
        Aircraft aircraft = repository.findByIdForUpdate(new AircraftId(command.id()))
                .orElseThrow(() -> notFound(command.id()));
        int assignedSeats = seats.maxAssignedSeatsByAircraft(aircraft.getId());
        if (capacity.value() < assignedSeats) {
            throw new CustomException("AIRCRAFT_CAPACITY_BELOW_ASSIGNED_SEATS", ErrorType.CONFLICT,
                    "Capacity cannot be below the maximum assigned seat count of a flight: " + assignedSeats);
        }
        aircraft.changeModel(model);
        aircraft.changeCapacity(capacity, assignedSeats);
        return AircraftView.from(repository.save(aircraft));
    }

    @Override
    public AircraftView getById(UUID id) {
        return repository.findById(new AircraftId(id)).map(AircraftView::from)
                .orElseThrow(() -> notFound(id));
    }

    @Override
    public List<AircraftView> list(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidDomainValueException("Page must be >= 0 and size must be between 1 and 100");
        }
        return repository.findAll(page, size).stream().map(AircraftView::from).toList();
    }

    private CustomException notFound(UUID id) {
        return new CustomException("AIRCRAFT_NOT_FOUND", ErrorType.NOT_FOUND, "Aircraft not found: " + id);
    }
}
