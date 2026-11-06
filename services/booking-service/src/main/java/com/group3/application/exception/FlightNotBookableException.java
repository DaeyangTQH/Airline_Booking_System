package com.group3.application.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class FlightNotBookableException extends CustomException {
    public FlightNotBookableException(String message) {

        super(
                "FLIGHT_NOT_BOOKABLE",
                ErrorType.CONFLICT,
                message
        );
    }
}
