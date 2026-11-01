package com.group3.domain.aggregate;

import com.group3.domain.exception.InvalidDomainValueException;
import com.group3.domain.vo.BookingPassengerId;
import com.group3.domain.vo.ContactInfo;
import com.group3.domain.vo.PassengerName;
import com.group3.domain.vo.PassengerProfileId;
import com.group3.domain.vo.PassportNumber;

import java.time.LocalDate;

public final class BookingPassenger {

    private final BookingPassengerId id;
    private final PassengerProfileId passengerProfileId;
    private final PassengerName fullName;
    private final PassportNumber passportNumber;
    private final LocalDate dateOfBirth;
    private final ContactInfo contactInfo;

    private BookingPassenger(
            BookingPassengerId id,
            PassengerProfileId passengerProfileId,
            PassengerName fullName,
            PassportNumber passportNumber,
            LocalDate dateOfBirth,
            ContactInfo contactInfo
    ) {
        this.id = id;
        this.passengerProfileId = passengerProfileId;
        this.fullName = fullName;
        this.passportNumber = passportNumber;
        this.dateOfBirth = dateOfBirth;
        this.contactInfo = contactInfo;
    }

    public static BookingPassenger create(
            BookingPassengerId id,
            PassengerProfileId passengerProfileId,
            PassengerName fullName,
            PassportNumber passportNumber,
            LocalDate dateOfBirth,
            ContactInfo contactInfo
    ) {
        if (id == null) {
            throw new InvalidDomainValueException(
                    "Booking passenger ID must not be null"
            );
        }

        if (fullName == null) {
            throw new InvalidDomainValueException(
                    "Passenger name must not be null"
            );
        }

        if (dateOfBirth == null) {
            throw new InvalidDomainValueException(
                    "Passenger date of birth must not be null"
            );
        }

        return new BookingPassenger(
                id,
                passengerProfileId,
                fullName,
                passportNumber,
                dateOfBirth,
                contactInfo
        );
    }

    public BookingPassengerId getId() {
        return id;
    }

    public PassengerProfileId getPassengerProfileId() {
        return passengerProfileId;
    }

    public PassengerName getFullName() {
        return fullName;
    }

    public PassportNumber getPassportNumber() {
        return passportNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public ContactInfo getContactInfo() {
        return contactInfo;
    }
}
