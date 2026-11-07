package com.group3.application.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class BookingAccessDeniedException extends CustomException {

    public BookingAccessDeniedException(String message) {

        super(
                "BOOKING_ACCESS_DENIED",
                ErrorType.FORBIDDEN,
                message
        );
    }
}
