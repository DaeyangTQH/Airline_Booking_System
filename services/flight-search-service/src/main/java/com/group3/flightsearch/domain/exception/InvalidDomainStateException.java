package com.group3.flightsearch.domain.exception;

public class InvalidDomainStateException extends IllegalStateException {
    public InvalidDomainStateException(String message) {
        super(message);
    }
}
