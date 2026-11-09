package com.group3.infrastructure.adapter;

import com.group3.application.port.out.BookingQueryPort;
import com.group3.application.query.BookingDetailView;
import com.group3.application.query.BookingPassengerView;
import com.group3.application.query.BookingSummaryView;
import com.group3.infrastructure.persistence.BookingJpaRepository;
import com.group3.infrastructure.persistence.BookingPassengerJpaRepository;
import com.group3.infrastructure.persistence.TicketJpaRepository;
import com.group3.infrastructure.persistence.entity.BookingEntity;
import com.group3.infrastructure.persistence.entity.BookingPassengerEntity;
import com.group3.infrastructure.persistence.entity.TicketEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingQueryAdapter implements BookingQueryPort {

    private final BookingJpaRepository bookingJpaRepository;
    private final BookingPassengerJpaRepository bookingPassengerJpaRepository;
    private final TicketJpaRepository ticketJpaRepository;

    @Override
    public Optional<BookingDetailView> findDetailByIdAndUserId(
            UUID bookingId,
            UUID userId
    ) {
        return bookingJpaRepository
                .findByIdAndUserId(bookingId, userId)
                .map(this::toDetailView);
    }

    @Override
    public Optional<BookingDetailView> findDetailByReferenceAndUserId(
            String bookingReference,
            UUID userId
    ) {
        return bookingJpaRepository
                .findByBookingReferenceAndUserId(
                        bookingReference,
                        userId
                )
                .map(this::toDetailView);
    }

    @Override
    public List<BookingSummaryView> findSummariesByUserId(UUID userId) {
        return bookingJpaRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toSummaryView)
                .toList();
    }

    private BookingDetailView toDetailView(BookingEntity bookingEntity) {
        List<BookingPassengerEntity> passengerEntities =
                bookingPassengerJpaRepository.findAllByBooking_Id(bookingEntity.getId());

        Map<UUID, TicketEntity> ticketsByPassengerId =
                ticketJpaRepository.findAllByBookingId(bookingEntity.getId())
                        .stream()
                        .collect(Collectors.toMap(
                                ticket -> ticket.getPassenger().getId(),
                                Function.identity(),
                                (left, right) -> left
                        ));

        List<BookingPassengerView> passengers = passengerEntities.stream()
                .map(entity -> toPassengerView(entity, ticketsByPassengerId.get(entity.getId())))
                .toList();

        return new BookingDetailView(
                bookingEntity.getId(),
                bookingEntity.getBookingReference(),
                bookingEntity.getUserId(),
                bookingEntity.getFlightId(),
                bookingEntity.getFareClass(),
                bookingEntity.getTicketQuantity(),
                bookingEntity.getUnitPrice(),
                bookingEntity.getTotalAmount(),
                bookingEntity.getCurrency(),
                bookingEntity.getPhone(),
                bookingEntity.getEmail(),
                bookingEntity.getStatus(),
                bookingEntity.getCreatedAt(),
                bookingEntity.getExpiresAt(),
                passengers
        );
    }

    private BookingPassengerView toPassengerView(
            BookingPassengerEntity entity,
            TicketEntity ticket
    ) {
        return new BookingPassengerView(
                entity.getId(),
                entity.getPassengerProfileId(),
                entity.getFullName(),
                entity.getPassportNumber(),
                entity.getDateOfBirth(),
                entity.getPhone(),
                entity.getEmail(),
                ticket == null ? null : ticket.getId(),
                ticket == null ? null : ticket.getTicketNumber(),
                ticket == null ? null : ticket.getSeatId(),
                ticket == null ? null : ticket.getStatus()
        );
    }

    private BookingSummaryView toSummaryView(
            BookingEntity entity
    ) {
        return new BookingSummaryView(
                entity.getId(),
                entity.getBookingReference(),
                entity.getFlightId(),
                entity.getFareClass(),
                entity.getTicketQuantity(),
                entity.getTotalAmount(),
                entity.getCurrency(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getExpiresAt()
        );
    }
}
