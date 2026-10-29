package com.group3.domain.aggregate;

import com.group3.domain.exception.InvalidBookingStateException;
import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.BookingReference;
import com.group3.domain.vo.ContactInfo;
import com.group3.domain.vo.FareClass;
import com.group3.domain.vo.FlightId;
import com.group3.domain.vo.Money;
import com.group3.domain.vo.TicketQuantity;
import com.group3.domain.vo.UserId;

import java.time.Instant;

public class Booking {

    private final BookingId id;
    private final BookingReference reference;
    private final UserId userId;
    private final FlightId flightId;
    private final FareClass fareClass;
    private final TicketQuantity ticketQuantity;
    private final Money unitPrice;
    private final Money totalAmount;
    private final ContactInfo contactInfo;
    private final Instant createdAt;
    private final Instant expiresAt;

    private BookingStatus status;

    private Booking(
            BookingId id,
            BookingReference reference,
            UserId userId,
            FlightId flightId,
            FareClass fareClass,
            TicketQuantity ticketQuantity,
            Money unitPrice,
            Money totalAmount,
            ContactInfo contactInfo,
            BookingStatus status,
            Instant createdAt,
            Instant expiresAt
    ){
        this.id = id;
        this.reference = reference;
        this.userId = userId;
        this.flightId = flightId;
        this.fareClass = fareClass;
        this.ticketQuantity = ticketQuantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.contactInfo = contactInfo;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public static Booking create(
            BookingId id,
            BookingReference reference,
            UserId userId,
            FlightId flightId,
            FareClass fareClass,
            TicketQuantity ticketQuantity,
            Money unitPrice,
            ContactInfo contactInfo,
            Instant createdAt,
            Instant expiresAt
    ){
        validateRequiredFields(
                id,
                reference,
                userId,
                flightId,
                fareClass,
                ticketQuantity,
                unitPrice,
                contactInfo,
                createdAt,
                expiresAt
        );

        if(!expiresAt.isAfter(createdAt)){
            throw new InvalidDomainValueException(
                    "Booking expiration time must be after creation time"
            );
        }

        Money totalAmount = unitPrice.multiply(ticketQuantity);

        return new Booking(
                id,
                reference,
                userId,
                flightId,
                fareClass,
                ticketQuantity,
                unitPrice,
                totalAmount,
                contactInfo,
                BookingStatus.PENDING,
                createdAt,
                expiresAt
        );
    }

    private static void validateRequiredFields(
            BookingId id,
            BookingReference reference,
            UserId userId,
            FlightId flightId,
            FareClass fareClass,
            TicketQuantity ticketQuantity,
            Money unitPrice,
            ContactInfo contactInfo,
            Instant createdAt,
            Instant expiresAt
    ){
        if (id == null) {
            throw new InvalidDomainValueException(
                    "Booking ID must not be null"
            );
        }

        if (reference == null) {
            throw new InvalidDomainValueException(
                    "Booking reference must not be null"
            );
        }

        if (userId == null) {
            throw new InvalidDomainValueException(
                    "User ID must not be null"
            );
        }

        if (flightId == null) {
            throw new InvalidDomainValueException(
                    "Flight ID must not be null"
            );
        }

        if (fareClass == null) {
            throw new InvalidDomainValueException(
                    "Fare class must not be null"
            );
        }

        if (ticketQuantity == null) {
            throw new InvalidDomainValueException(
                    "Ticket quantity must not be null"
            );
        }

        if (unitPrice == null) {
            throw new InvalidDomainValueException(
                    "Unit price must not be null"
            );
        }

        if (contactInfo == null) {
            throw new InvalidDomainValueException(
                    "Contact information must not be null"
            );
        }

        if (createdAt == null) {
            throw new InvalidDomainValueException(
                    "Creation time must not be null"
            );
        }

        if (expiresAt == null) {
            throw new InvalidDomainValueException(
                    "Expiration time must not be null"
            );
        }
    }

    public void confirmPayment(){
        if (status == BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException(
                    "Cannot confirm payment for a cancelled booking"
            );
        }

        if(status == BookingStatus.CONFIRMED){
            return;
        }

        status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        if(status == BookingStatus.CANCELLED){
            return;
        }
        status = BookingStatus.CANCELLED;
    }

    public boolean isExpired(Instant now){
        if(now == null){
            throw new InvalidDomainValueException(
                    "Current time must not be null"
            );
        }
        return status == BookingStatus.PENDING
                && !now.isBefore(expiresAt);
    }

    public void expire(Instant now){
        if(isExpired(now)){
            status = BookingStatus.CANCELLED;
        }
    }

    public BookingId getId() {
        return id;
    }

    public BookingReference getReference(){
        return reference;
    }

    public UserId getUserId() {
        return userId;
    }

    public FlightId getFlightId() {
        return flightId;
    }

    public FareClass getFareClass() {
        return fareClass;
    }

    public TicketQuantity getTicketQuantity() {
        return ticketQuantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
