package com.group3.domain.exception;

public class DomainValidationException extends RuntimeException {
    public DomainValidationException(String message) { super(message); }
}
