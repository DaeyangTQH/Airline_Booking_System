package com.group3.flightsearch.domain.valueobject;

import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import java.util.Locale;

public record AirportCode(String value) {
    public AirportCode {
        if (value == null) {
            throw new InvalidDomainValueException("AirportCode must not be null");
        }
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!value.matches("[A-Z]{3}")) {
            throw new InvalidDomainValueException("AirportCode must have three letters");
        }
    }
}
