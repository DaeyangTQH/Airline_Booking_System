package com.group3.domain.aggregate;

import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.exception.InvalidTicketStateException;
import com.group3.domain.vo.BookingPassengerId;
import com.group3.domain.vo.SeatId;
import com.group3.domain.vo.TicketId;
import com.group3.domain.vo.TicketNumber;

public class Ticket {

    private final TicketId id;
    private final TicketNumber number;
    private final BookingPassengerId passengerId;

    private SeatId seatId;
    private TicketStatus status;

    private Ticket(
            TicketId id,
            TicketNumber number,
            BookingPassengerId passengerId,
            SeatId seatId,
            TicketStatus status
    ) {
        this.id = id;
        this.number = number;
        this.passengerId = passengerId;
        this.seatId = seatId;
        this.status = status;
    }

    public static Ticket issue(
            TicketId id,
            TicketNumber number,
            BookingPassengerId passengerId
    ){
        if(id == null) {
            throw new InvalidDomainValueException(
                    "Ticket ID must not be null"
            );
        }

        if(number == null) {
            throw new InvalidDomainValueException(
                    "Ticket number must not be null"
            );
        }

        if(passengerId == null){
            throw new InvalidDomainValueException(
                    "Booking passenger ID must not be null"
            );
        }

        return new Ticket(
                id,
                number,
                passengerId,
                null,
                TicketStatus.ISSUED
        );
    }

    public static Ticket restore(
            TicketId id,
            TicketNumber number,
            BookingPassengerId passengerId,
            SeatId seatId,
            TicketStatus status
    ) {
        if (id == null) {
            throw new InvalidDomainValueException(
                    "Ticket ID must not be null"
            );
        }

        if (number == null) {
            throw new InvalidDomainValueException(
                    "Ticket number must not be null"
            );
        }

        if (passengerId == null) {
            throw new InvalidDomainValueException(
                    "Booking passenger ID must not be null"
            );
        }

        if (status == null) {
            throw new InvalidDomainValueException(
                    "Ticket status must not be null"
            );
        }

        return new Ticket(
                id,
                number,
                passengerId,
                seatId,
                status
        );
    }

    public void assignSeat(SeatId id){
        if(id == null){
            throw new InvalidDomainValueException(
                    "Seat ID must not be null"
            );
        }

        ensureIssued(
                "Only issued ticket can be assigned a seat"
        );

        this.seatId = id;
    }

    public void cancel() {
        if(status == TicketStatus.CANCELLED) {
            return;
        }

        if(status == TicketStatus.REFUNDED){
            throw new InvalidTicketStateException(
                    "Refunded ticket cannot be cancelled"
            );
        }

        status = TicketStatus.CANCELLED;
    }

    public void markRefunded() {
        if(status == TicketStatus.REFUNDED){
            return;
        }

        if(status == TicketStatus.CANCELLED){
            throw new InvalidTicketStateException(
                    "Cancelled ticket cannot be marked as refunded"
            );
        }

        status = TicketStatus.REFUNDED;
    }

    private void ensureIssued(String message){
        if(status != TicketStatus.ISSUED){
            throw new InvalidTicketStateException(
                    message
            );
        }
    }

    public TicketId getId() {
        return id;
    }

    public TicketNumber getTicketNumber() {
        return number;
    }

    public BookingPassengerId getBookingPassengerId() {
        return passengerId;
    }

    public SeatId getSeatId() {
        return seatId;
    }

    public TicketStatus getStatus() {
        return status;
    }
}
