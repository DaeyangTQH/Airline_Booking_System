package com.group3.flightsearch.domain.aggregate;

import com.group3.flightsearch.domain.exception.InvalidDomainStateException;
import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import com.group3.flightsearch.domain.model.FareClass;
import com.group3.flightsearch.domain.model.SeatStatus;
import com.group3.flightsearch.domain.valueobject.BookingId;
import com.group3.flightsearch.domain.valueobject.FlightId;
import com.group3.flightsearch.domain.valueobject.SeatId;
import com.group3.flightsearch.domain.valueobject.SeatNumber;
import java.time.Instant;

public final class Seat {
    private final SeatId id;
    private final FlightId flightId;
    private final SeatNumber number;
    private final FareClass seatClass;
    private SeatStatus status;
    private BookingId heldByBookingId;
    private Instant holdExpiresAt;

    private Seat(SeatId id, FlightId flightId, SeatNumber number, FareClass seatClass,
                 SeatStatus status, BookingId heldByBookingId, Instant holdExpiresAt) {
        if (id == null || flightId == null || number == null || seatClass == null || status == null) {
            throw new InvalidDomainValueException("Seat identity, flight, number, class and status are required");
        }
        if (status == SeatStatus.HELD) {
            if (heldByBookingId == null || holdExpiresAt == null) {
                throw new InvalidDomainValueException("Held seat requires owner and expiry");
            }
        } else if (heldByBookingId != null || holdExpiresAt != null) {
            throw new InvalidDomainValueException("Only a held seat may have owner or expiry");
        }
        this.id = id;
        this.flightId = flightId;
        this.number = number;
        this.seatClass = seatClass;
        this.status = status;
        this.heldByBookingId = heldByBookingId;
        this.holdExpiresAt = holdExpiresAt;
    }

    public static Seat create(SeatId id, FlightId flightId, SeatNumber number, FareClass seatClass) {
        return new Seat(id, flightId, number, seatClass, SeatStatus.AVAILABLE, null, null);
    }

    public static Seat restore(SeatId id, FlightId flightId, SeatNumber number, FareClass seatClass,
                               SeatStatus status, BookingId owner, Instant expiresAt) {
        return new Seat(id, flightId, number, seatClass, status, owner, expiresAt);
    }

    public void hold(BookingId bookingId, Instant expiresAt, Instant now) {
        if (bookingId == null || expiresAt == null || now == null || !expiresAt.isAfter(now)) {
            throw new InvalidDomainValueException("Hold requires booking id and future expiry");
        }
        expireHold(now);
        if (status != SeatStatus.AVAILABLE) {
            throw new InvalidDomainStateException("Seat is not available");
        }
        status = SeatStatus.HELD;
        heldByBookingId = bookingId;
        holdExpiresAt = expiresAt;
    }

    public void confirm(BookingId bookingId, Instant now) {
        requireOwner(bookingId);
        if (now == null) {
            throw new InvalidDomainValueException("Current time is required");
        }
        if (!now.isBefore(holdExpiresAt)) {
            throw new InvalidDomainStateException("Seat hold has expired");
        }
        status = SeatStatus.BOOKED;
        clearHold();
    }

    public void release(BookingId bookingId) {
        requireOwner(bookingId);
        status = SeatStatus.AVAILABLE;
        clearHold();
    }

    public void block() {
        if (status != SeatStatus.AVAILABLE) {
            throw new InvalidDomainStateException("Only an available seat can be blocked");
        }
        status = SeatStatus.BLOCKED;
    }

    public void unblock() {
        if (status != SeatStatus.BLOCKED) {
            throw new InvalidDomainStateException("Only a blocked seat can be unblocked");
        }
        status = SeatStatus.AVAILABLE;
    }

    public boolean expireHold(Instant now) {
        if (now == null) {
            throw new InvalidDomainValueException("Current time is required");
        }
        if (status == SeatStatus.HELD && !now.isBefore(holdExpiresAt)) {
            status = SeatStatus.AVAILABLE;
            clearHold();
            return true;
        }
        return false;
    }

    private void requireOwner(BookingId bookingId) {
        if (bookingId == null) {
            throw new InvalidDomainValueException("Booking id is required");
        }
        if (status != SeatStatus.HELD || !bookingId.equals(heldByBookingId)) {
            throw new InvalidDomainStateException("Seat is not held by this booking");
        }
    }

    private void clearHold() {
        heldByBookingId = null;
        holdExpiresAt = null;
    }

    public SeatId getId() { return id; }
    public FlightId getFlightId() { return flightId; }
    public SeatNumber getNumber() { return number; }
    public FareClass getSeatClass() { return seatClass; }
    public SeatStatus getStatus() { return status; }
    public BookingId getHeldByBookingId() { return heldByBookingId; }
    public Instant getHoldExpiresAt() { return holdExpiresAt; }
}
