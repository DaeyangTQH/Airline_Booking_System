package com.group3.infrastructure.adapter;

import com.group3.domain.aggregate.Ticket;
import com.group3.domain.exception.PassengerNotFoundException;
import com.group3.domain.repository.TicketRepository;
import com.group3.domain.vo.BookingPassengerId;
import com.group3.domain.vo.TicketId;
import com.group3.domain.vo.TicketNumber;
import com.group3.infrastructure.persistence.BookingPassengerJpaRepository;
import com.group3.infrastructure.persistence.TicketJpaRepository;
import com.group3.infrastructure.persistence.entity.BookingPassengerEntity;
import com.group3.infrastructure.persistence.entity.TicketEntity;
import com.group3.infrastructure.persistence.mapper.TicketPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TicketRepositoryAdapter implements TicketRepository {

    private final TicketJpaRepository ticketJpaRepository;
    private final BookingPassengerJpaRepository passengerJpaRepository;
    private final TicketPersistenceMapper mapper;

    @Override
    @Transactional
    public Ticket save(Ticket ticket) {
        BookingPassengerEntity passengerEntity = passengerJpaRepository.findById(ticket.getBookingPassengerId().value())
                .orElseThrow(() -> new PassengerNotFoundException(
                        "Passenger not found: "+ ticket.getBookingPassengerId().value())
        );

        TicketEntity ticketEntity = mapper.toEntity(ticket, passengerEntity);
        TicketEntity savedEntity = ticketJpaRepository.save(ticketEntity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ticket> findById(TicketId id) {
        return ticketJpaRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ticket> findByNumber(TicketNumber number) {
        return ticketJpaRepository.findByTicketNumber(number.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Ticket> findByPassengerId(BookingPassengerId passengerId) {
        return ticketJpaRepository.findByPassenger_Id(passengerId.value())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByPassengerId(BookingPassengerId passengerId) {
        return ticketJpaRepository.existsByPassenger_Id(passengerId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNumber(TicketNumber number) {
        return ticketJpaRepository.existsByTicketNumber(number.value());
    }
}
