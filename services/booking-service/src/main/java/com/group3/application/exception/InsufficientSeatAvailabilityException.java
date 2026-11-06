package com.group3.application.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class InsufficientSeatAvailabilityException extends CustomException {
    public InsufficientSeatAvailabilityException(String message) {

        super(
                "INSUFFICIENT_SEAT_AVAILABILITY",
                ErrorType.CONFLICT,
                message
        );
    }
}
