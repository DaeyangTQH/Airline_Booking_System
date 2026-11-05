package com.group3.flightsearch.domain.exception;

public class InvalidDomainValueException extends IllegalArgumentException {
    public InvalidDomainValueException(String message) {
        super(message);
    }
}
