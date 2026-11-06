package com.group3.application.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class FlightSearchCommunicationException extends CustomException {

    public FlightSearchCommunicationException(String message){

        super(
                "FLIGHT_SEARCH_COMMUNICATION_FAILED",
                ErrorType.INTERNAL,
                message
        );
    }
}
