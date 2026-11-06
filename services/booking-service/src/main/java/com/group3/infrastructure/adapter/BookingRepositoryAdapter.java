package com.group3.infrastructure.adapter;

import com.group3.domain.aggregate.Booking;
import com.group3.domain.repository.BookingRepository;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.BookingReference;
import com.group3.domain.vo.UserId;
import com.group3.infrastructure.persistence.BookingJpaRepository;
import com.group3.infrastructure.persistence.BookingPassengerJpaRepository;
import com.group3.infrastructure.persistence.entity.BookingEntity;
import com.group3.infrastructure.persistence.entity.BookingPassengerEntity;
import com.group3.infrastructure.persistence.mapper.BookingPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BookingRepositoryAdapter implements BookingRepository {

    private final BookingJpaRepository bookingJpaRepository;
    private final BookingPassengerJpaRepository passengerJpaRepository;
    private final BookingPersistenceMapper mapper;

    @Override
    @Transactional
    public Booking save(Booking booking) {

        BookingEntity bookingEntity = mapper.toEntity(booking);
        BookingEntity savedBooking = bookingJpaRepository.save(bookingEntity);
        List<BookingPassengerEntity> passengerEntities =
                booking.getPassengers().stream()
                        .map(passenger ->
                            mapper.toPassengerEntity(passenger, savedBooking)
                                    ).toList();

        List<BookingPassengerEntity> savedPassengers = passengerJpaRepository.saveAll(passengerEntities);

        return mapper.toDomain(savedBooking, savedPassengers);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> findById(BookingId id) {
        return bookingJpaRepository.findById(id.value())
                .map(this::loadAggregate);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> findByReference(BookingReference reference) {
        return bookingJpaRepository.findByBookingReference(reference.value())
                .map(this::loadAggregate);
    }

    @Override
    @Transactional
    public List<Booking> findAllByUserId(UserId userId) {
        return bookingJpaRepository.findAllByUserId(userId.value())
                .stream()
                .map(this::loadAggregate)
                .toList();
    }

    @Override
    @Transactional
    public Optional<Booking> findByIdForUpdate(BookingId id) {
        return bookingJpaRepository
                .findByIdForUpdate(id.value())
                .map(this::loadAggregate);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByReference(BookingReference reference) {
        return bookingJpaRepository.existsByBookingReference(reference.value());
    }

    private Booking loadAggregate(BookingEntity bookingEntity){
        List<BookingPassengerEntity> passengers =
                passengerJpaRepository.findAllByBooking_Id(bookingEntity.getId());
        return mapper.toDomain(bookingEntity, passengers);
    }
}
