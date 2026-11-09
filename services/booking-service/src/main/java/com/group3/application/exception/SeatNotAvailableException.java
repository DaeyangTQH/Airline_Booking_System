package com.group3.application.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class SeatNotAvailableException
        extends CustomException {

    public SeatNotAvailableException(
            String message
    ) {
        super(
                "SEAT_NOT_AVAILABLE",
                ErrorType.CONFLICT,
                message
        );
    }
}
