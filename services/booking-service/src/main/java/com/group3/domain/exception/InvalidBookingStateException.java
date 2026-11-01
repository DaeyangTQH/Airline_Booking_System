package com.group3.domain.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class InvalidBookingStateException extends CustomException {

    public InvalidBookingStateException(String message) {
        super(
                "INVALID_BOOKING_STATE",
                ErrorType.CONFLICT,
                message
        );
    }
}
