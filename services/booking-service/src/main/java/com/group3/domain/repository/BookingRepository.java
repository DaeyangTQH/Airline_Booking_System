package com.group3.domain.repository;

import com.group3.domain.aggregate.Booking;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.BookingPassengerId;
import com.group3.domain.vo.BookingReference;
import com.group3.domain.vo.UserId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(BookingId id);

    Optional<Booking> findByIdForUpdate(BookingId id);

    boolean existsByReference(BookingReference reference);

    int expirePendingBookings(Instant now);

    Optional<Booking> findByPassengerId(BookingPassengerId passengerId);

}
