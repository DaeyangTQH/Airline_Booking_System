package com.group3.domain.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class PassengerNotFoundException extends CustomException {

    public PassengerNotFoundException(String message){
        super(
                "PASSENGER_NOT_FOUND",
                ErrorType.NOT_FOUND,
                message
        );
    }
}
