package com.group3.application.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class FlightSeatCommunicationException
        extends CustomException {

    public FlightSeatCommunicationException(
            String message
    ) {
        super(
                "FLIGHT_SEAT_COMMUNICATION_FAILED",
                ErrorType.INTERNAL,
                message
        );
    }
}
