package com.group3.application.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class FlightNotFoundException extends CustomException {

    public FlightNotFoundException(String message){

        super(
                "FLIGHT_NOT_FOUND",
                ErrorType.NOT_FOUND,
                message
        );
    }
}
