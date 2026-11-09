package com.group3.domain.aggregate;

import com.group3.domain.exception.InvalidBookingStateException;
import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.exception.PassengerAlreadyExistsException;
import com.group3.domain.exception.PassengerLimitExceededException;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.BookingReference;
import com.group3.domain.vo.ContactInfo;
import com.group3.domain.vo.FareClass;
import com.group3.domain.vo.FlightId;
import com.group3.domain.vo.Money;
import com.group3.domain.vo.PaymentId;
import com.group3.domain.vo.TicketQuantity;
import com.group3.domain.vo.UserId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


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

    private final List<BookingPassenger> passengers;

    private BookingStatus status;
    private PaymentId paymentId;

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
            Instant expiresAt,
            PaymentId paymentId,
            List<BookingPassenger> passengers
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
        this.paymentId = paymentId;
        this.passengers = new ArrayList<>(passengers);
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
                expiresAt,
                null,
                List.of()
        );
    }

    public static Booking restore(
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
            Instant expiresAt,
            PaymentId paymentId,
            List<BookingPassenger> passengers
    ) {
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

        if (totalAmount == null) {
            throw new InvalidDomainValueException(
                    "Total amount must not be null"
            );
        }

        if (status == null) {
            throw new InvalidDomainValueException(
                    "Booking status must not be null"
            );
        }

        if (passengers == null) {
            throw new InvalidDomainValueException(
                    "Passenger list must not be null"
            );
        }

        if (!expiresAt.isAfter(createdAt)) {
            throw new InvalidDomainValueException(
                    "Booking expiration time must be after creation time"
            );
        }

        Money expectedTotal =
                unitPrice.multiply(ticketQuantity);

        if (!expectedTotal.equals(totalAmount)) {
            throw new InvalidDomainValueException(
                    "Stored total amount does not match unit price and ticket quantity"
            );
        }

        if (passengers.size() > ticketQuantity.value()) {
            throw new PassengerLimitExceededException(
                    "Stored passenger count exceeds ticket quantity"
            );
        }

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
                status,
                createdAt,
                expiresAt,
                paymentId,
                passengers
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

    public void attachPayment(PaymentId paymentId){
        if(paymentId == null) {
            throw new InvalidDomainValueException(
                    "Payment ID must not be null"
            );
        }

        if (this.paymentId != null) {
            if (this.paymentId.equals(paymentId)) {
                return;
            }

            throw new InvalidBookingStateException(
                    "Booking is already linked to another payment"
            );
        }

        if(status != BookingStatus.PENDING){
            throw new InvalidBookingStateException(
                    "Payment can only be attached to a pending booking"
            );
        }

        this.paymentId = paymentId;
    }

    public void ensureCanInitiatePayment(Instant now) {

        if(now == null){
            throw new InvalidDomainValueException(
                    "Current time must not be null"
            );
        }

        if(status != BookingStatus.PENDING){
            throw new InvalidBookingStateException(
                    "Booking đã hết hạn hoặc không còn chờ thanh toán."
            );
        }

        if(!now.isBefore(expiresAt)){
            throw new InvalidBookingStateException(
                    "Booking đã hết hạn giữ chỗ, không thể thanh toán. Vui lòng tạo đặt chỗ mới."
            );
        }
    }

    public void confirmPayment(
            PaymentId paymentId,
            Money paidAmount,
            Instant now
    ){

        if (now == null) {
            throw new InvalidDomainValueException(
                    "Current time must not be null"
            );
        }

        if(paymentId == null) {
            throw new InvalidDomainValueException(
                    "Payment ID must not be null"
            );
        }

        if(paidAmount == null) {
            throw new InvalidDomainValueException(
                    "Paid amount must not be null"
            );
        }

        if(this.paymentId == null) {
            throw new InvalidBookingStateException(
                    "Booking is not linked to a payment"
            );
        }

        if(!this.paymentId.equals(paymentId)) {
            throw new InvalidBookingStateException(
                    "Payment does not belong to this booking"
            );
        }

        if (!totalAmount.equals(paidAmount)) {
            throw new InvalidBookingStateException(
                    "Payment amount or currency does not match booking total"
            );
        }

        if (status == BookingStatus.CANCELLED) {
            throw new InvalidBookingStateException(
                    "Cannot confirm payment for a cancelled booking"
            );
        }

        if (status == BookingStatus.CONFIRMED) {
            return;
        }

        if (!now.isBefore(expiresAt)) {
            throw new InvalidBookingStateException(
                    "Booking đã hết hạn giữ chỗ, không thể xác nhận thanh toán."
            );
        }

        status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        if(status == BookingStatus.CANCELLED){
            return;
        }

        if (status == BookingStatus.CONFIRMED) {
            throw new InvalidBookingStateException(
                    "Confirmed booking cannot be cancelled"
            );
        }

        status = BookingStatus.CANCELLED;
    }

    public void addPassenger(BookingPassenger passenger){
        if (passenger == null){
            throw new InvalidDomainValueException(
                    "Passenger must not be null"
            );
        }

        if(status != BookingStatus.CONFIRMED){
            throw new InvalidBookingStateException(
                    "Only confirmed booking can be add passengers"
            );
        }

        boolean passengerAlreadyExists =
                passengers.stream()
                        .anyMatch(existing ->
                                isSamePassenger(
                                        existing,
                                        passenger
                                )
                        );

        if (passengerAlreadyExists) {
            throw new PassengerAlreadyExistsException(
                    "Passenger already exists in this booking"
            );
        }

        if(passengers.size() >= ticketQuantity.value()){
            throw new PassengerLimitExceededException(
                    "Passenger count cannot exceed ticket quantity"
            );
        }

        passengers.add(passenger);
    }

    private boolean isSamePassenger(
            BookingPassenger existing,
            BookingPassenger candidate
    ){

        if(existing.getId()
                .equals(candidate.getId())){
            return true;
        }

        if(existing.getPassportNumber()
                .equals(candidate.getPassportNumber())){
            return true;
        }

        return existing.getPassengerProfileId() != null
                && candidate.getPassengerProfileId() != null
                && existing.getPassengerProfileId()
                .equals(
                        candidate.getPassengerProfileId()
                );
    }

    public BookingId getId() {
        return id;
    }

    public BookingReference getReference(){
        return reference;
    }

    public UserId getUserId()  {
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

    public PaymentId getPaymentId() {
        return paymentId;
    }

    public List<BookingPassenger> getPassengers(){
        return List.copyOf(passengers);
    }
}
