package com.group3.domain.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class PassengerLimitExceededException extends CustomException {

    public PassengerLimitExceededException(String message){
        super(
                "PASSENGER_LIMIT_EXCEEDED",
                ErrorType.CONFLICT,
                message
        );
    }
}
