package com.group3.application.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class BookingReferenceGenerationException extends CustomException {
    public BookingReferenceGenerationException(String message) {
        super(
                "BOOKING_REFERENCE_GENERATION_FAILED",
                ErrorType.INTERNAL,
                message
        );
    }
}
