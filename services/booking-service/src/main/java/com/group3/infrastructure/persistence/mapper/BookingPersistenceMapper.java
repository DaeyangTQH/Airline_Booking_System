package com.group3.infrastructure.persistence.mapper;

import com.group3.domain.aggregate.Booking;
import com.group3.domain.aggregate.BookingPassenger;
import com.group3.domain.vo.BookingId;
import com.group3.domain.vo.BookingPassengerId;
import com.group3.domain.vo.BookingReference;
import com.group3.domain.vo.ContactInfo;
import com.group3.domain.vo.FlightId;
import com.group3.domain.vo.Money;
import com.group3.domain.vo.PassengerName;
import com.group3.domain.vo.PassengerProfileId;
import com.group3.domain.vo.PassportNumber;
import com.group3.domain.vo.PaymentId;
import com.group3.domain.vo.TicketQuantity;
import com.group3.domain.vo.UserId;
import com.group3.infrastructure.persistence.entity.BookingEntity;
import com.group3.infrastructure.persistence.entity.BookingPassengerEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class BookingPersistenceMapper {

    public BookingEntity toEntity(Booking booking){
        Objects.requireNonNull(booking, "Booking cannot be null");

        BookingEntity entity = new BookingEntity();

        entity.setId(booking.getId().value());
        entity.setBookingReference(booking.getReference().value());
        entity.setUserId(booking.getUserId().value());
        entity.setFlightId(booking.getFlightId().value());
        if(booking.getPaymentId() != null) {
            entity.setPaymentId(booking.getPaymentId().value());
        }
        entity.setFareClass(booking.getFareClass());
        entity.setTicketQuantity(booking.getTicketQuantity().value());
        entity.setUnitPrice(booking.getUnitPrice().amount());
        entity.setTotalAmount(booking.getTotalAmount().amount());
        entity.setCurrency(booking.getUnitPrice().currency());
        entity.setPhone(booking.getContactInfo().phone());
        entity.setEmail(booking.getContactInfo().email());
        entity.setStatus(booking.getStatus());
        entity.setExpiresAt(booking.getExpiresAt());
        entity.setCreatedAt(booking.getCreatedAt());

        return entity;
    }

    public BookingPassengerEntity toPassengerEntity(BookingPassenger passenger, BookingEntity booking){

        Objects.requireNonNull(passenger, "BookingPassenger must not be null");

        Objects.requireNonNull(booking, "BookingEntity must not be null");

        BookingPassengerEntity entity = new BookingPassengerEntity();

        entity.setId(passenger.getId().value());
        entity.setBooking(booking);

        if(passenger.getPassengerProfileId() != null) {
            entity.setPassengerProfileId(passenger.getPassengerProfileId().value());
        }

        entity.setFullName(passenger.getFullName().value());

        entity.setPassportNumber(passenger.getPassportNumber().value());
        entity.setDateOfBirth(passenger.getDateOfBirth());

        if(passenger.getContactInfo() != null) {
            entity.setPhone(passenger.getContactInfo().phone());
            entity.setEmail(passenger.getContactInfo().email());
        }

        return entity;
    }

    public Booking toDomain(
            BookingEntity bookingEntity,
            List<BookingPassengerEntity> passengerEntities
    ){

        Objects.requireNonNull(bookingEntity, "BookingEntity must not be null");

        Objects.requireNonNull(passengerEntities, "PassengerEntities must not be null");

        List<BookingPassenger> passengers = passengerEntities.stream()
                .map(this::toPassengerDomain)
                .toList();

        return Booking.restore(
                new BookingId(bookingEntity.getId()),
                new BookingReference(bookingEntity.getBookingReference()),
                new UserId(bookingEntity.getUserId()),
                new FlightId(bookingEntity.getFlightId()),
                bookingEntity.getFareClass(),
                new TicketQuantity(bookingEntity.getTicketQuantity()),
                new Money(bookingEntity.getUnitPrice(), bookingEntity.getCurrency()),
                new Money(bookingEntity.getTotalAmount(), bookingEntity.getCurrency()),
                new ContactInfo(bookingEntity.getPhone(), bookingEntity.getEmail()),
                bookingEntity.getStatus(),
                bookingEntity.getCreatedAt(),
                bookingEntity.getExpiresAt(),
                bookingEntity.getPaymentId() != null ? new PaymentId(bookingEntity.getPaymentId()) : null,
                passengers
        );
    }

    public BookingPassenger toPassengerDomain(BookingPassengerEntity entity){

        Objects.requireNonNull(entity, "BookingPassengerEntity must not be null");

        PassengerProfileId passengerProfileId = null;

        if(entity.getPassengerProfileId() != null) {
            passengerProfileId = new PassengerProfileId(entity.getPassengerProfileId());
        }

        PassportNumber passportNumber = null;

        if(entity.getPassportNumber() != null){
            passportNumber = new PassportNumber(entity.getPassportNumber());
        }

        ContactInfo contactInfo = null;

        if(entity.getPhone() != null || entity.getEmail() != null){
            contactInfo = new ContactInfo(entity.getPhone(), entity.getEmail());
        }

        return BookingPassenger.create(
                new BookingPassengerId(entity.getId()),
                passengerProfileId,
                new PassengerName(entity.getFullName()),
                passportNumber,
                entity.getDateOfBirth(),
                contactInfo
        );
    }
}
