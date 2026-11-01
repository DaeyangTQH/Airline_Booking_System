package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

public record BookingReference(String value) {

    private static final String PATTERN = "[A-Z0-9]{6}";

    public BookingReference {
        if(value == null || !value.matches(PATTERN)){
            throw new InvalidDomainValueException(
                    "Booking reference must contain exactly 6 uppercase letters or digits"
            );
        }
    }
}
