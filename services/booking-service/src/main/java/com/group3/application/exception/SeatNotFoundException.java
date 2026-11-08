package com.group3.application.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class SeatNotFoundException
        extends CustomException {

    public SeatNotFoundException(
            String message
    ) {
        super(
                "SEAT_NOT_FOUND",
                ErrorType.NOT_FOUND,
                message
        );
    }
}
