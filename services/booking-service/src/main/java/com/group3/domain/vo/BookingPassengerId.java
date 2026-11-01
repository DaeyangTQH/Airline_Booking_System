package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

import java.util.UUID;

public record BookingPassengerId(UUID value) {

    public BookingPassengerId{
        if(value == null) {
            throw new InvalidDomainValueException(
                    "Booking Passenger ID must not be null"
            );
        }
    }

    public static BookingPassengerId generate() {
        return new BookingPassengerId(UUID.randomUUID());
    }
}
