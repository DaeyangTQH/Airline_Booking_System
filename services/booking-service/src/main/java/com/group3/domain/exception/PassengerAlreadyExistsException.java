package com.group3.domain.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class PassengerAlreadyExistsException extends CustomException {

    public PassengerAlreadyExistsException(String message){
        super(
                "PASSENGER_ALREADY_EXISTS",
                ErrorType.CONFLICT,
                message
        );
    }
}
