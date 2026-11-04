package com.group3.domain.repository;

import com.group3.domain.aggregate.Booking;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.BookingReference;
import com.group3.domain.vo.UserId;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(BookingId id);

    Optional<Booking> findByReference(BookingReference reference);

    List<Booking> findAllByUserId(UserId userId);

    boolean existsByReference(BookingReference reference);
}
