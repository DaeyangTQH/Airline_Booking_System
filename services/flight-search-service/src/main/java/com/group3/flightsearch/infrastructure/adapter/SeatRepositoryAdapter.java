package com.group3.flightsearch.infrastructure.adapter;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;
import com.group3.flightsearch.application.port.out.FlightAvailabilityPort;
import com.group3.flightsearch.domain.aggregate.Seat;
import com.group3.flightsearch.domain.model.FareClass;
import com.group3.flightsearch.domain.repository.SeatRepository;
import com.group3.flightsearch.domain.valueobject.*;
import com.group3.flightsearch.infrastructure.persistence.mapper.SeatPersistenceMapper;
import com.group3.flightsearch.infrastructure.persistence.repository.FlightJpaRepository;
import com.group3.flightsearch.infrastructure.persistence.repository.SeatJpaRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SeatRepositoryAdapter implements SeatRepository {
    private final SeatJpaRepository repository;
    private final FlightJpaRepository flights;
    private final SeatPersistenceMapper mapper;
    private final FlightAvailabilityPort availability;

    @Override
    public Seat save(Seat seat) {
        var entity = mapper.toEntity(seat);
        entity.setFlight(flights.getReferenceById(seat.getFlightId().value()));
        try {
            var saved = mapper.toDomain(repository.saveAndFlush(entity));
            availability.refresh(seat.getFlightId().value());
            return saved;
        } catch (DataIntegrityViolationException exception) {
            for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
                if (cause instanceof ConstraintViolationException violation
                        && "uk_seats_assigned_ticket".equalsIgnoreCase(violation.getConstraintName())) {
                    throw new CustomException("TICKET_ALREADY_ASSIGNED", ErrorType.CONFLICT,
                            "Ticket already has a seat assignment");
                }
                if (cause instanceof ConstraintViolationException violation
                        && "uk_seats_flight_number".equalsIgnoreCase(violation.getConstraintName())) {
                    throw new CustomException("SEAT_NUMBER_ALREADY_EXISTS", ErrorType.CONFLICT,
                            "Seat already exists on this flight: " + seat.getNumber().value());
                }
            }
            throw exception;
        }
    }

    @Override
    public Optional<Seat> findById(SeatId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Seat> findByAssignedTicketId(TicketId ticketId) {
        return repository.findByAssignedTicketId(ticketId.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByFlightAndNumber(FlightId flightId, SeatNumber number) {
        return repository.existsByFlightIdAndSeatNumber(flightId.value(), number.value());
    }

    @Override
    public long countByFlight(FlightId flightId) {
        return repository.countByFlightId(flightId.value());
    }

    @Override
    public int maxAssignedSeatsByAircraft(AircraftId aircraftId) {
        return repository.maxAssignedSeatsByAircraft(aircraftId.value());
    }

    @Override
    public List<Seat> findByFlight(FlightId flightId, FareClass seatClass, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("seatNumber", "id"));
        var rows = seatClass == null ? repository.findAllByFlightId(flightId.value(), pageable)
                : repository.findAllByFlightIdAndSeatClass(flightId.value(), seatClass, pageable);
        return rows.stream().map(mapper::toDomain).toList();
    }
}
