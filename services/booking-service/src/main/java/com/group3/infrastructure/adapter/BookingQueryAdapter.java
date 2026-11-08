package com.group3.infrastructure.adapter;

import com.group3.application.query.BookingPassengerView;
import com.group3.application.port.out.BookingQueryPort;
import com.group3.application.query.BookingDetailView;
import com.group3.application.query.BookingSummaryView;
import com.group3.infrastructure.persistence.BookingJpaRepository;
import com.group3.infrastructure.persistence.BookingPassengerJpaRepository;
import com.group3.infrastructure.persistence.entity.BookingEntity;
import com.group3.infrastructure.persistence.entity.BookingPassengerEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingQueryAdapter implements BookingQueryPort {

    private final BookingJpaRepository bookingJpaRepository;
    private final BookingPassengerJpaRepository bookingPassengerJpaRepository;

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

    private BookingDetailView toDetailView(BookingEntity bookingEntity){
        List<BookingPassengerView> passengers =
                bookingPassengerJpaRepository
                        .findAllByBooking_Id(bookingEntity.getId())
                        .stream()
                        .map(this::toPassengerView)
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
            BookingPassengerEntity entity
    ){
        return new BookingPassengerView(
                entity.getId(),
                entity.getPassengerProfileId(),
                entity.getFullName(),
                entity.getPassportNumber(),
                entity.getDateOfBirth(),
                entity.getPhone(),
                entity.getEmail()
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
