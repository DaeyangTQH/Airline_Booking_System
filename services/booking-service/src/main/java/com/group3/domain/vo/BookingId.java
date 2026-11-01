package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

import java.util.UUID;

public record BookingId(UUID value) {
    public BookingId{
        if(value == null){
            throw new InvalidDomainValueException(
                    "Booking ID must not be null"
            );
        }
    }

    public static BookingId generate() {
        return new BookingId(UUID.randomUUID());
    }
}
