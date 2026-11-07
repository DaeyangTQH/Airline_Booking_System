package com.group3.application.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class TicketNumberGenerationException extends CustomException {

    public TicketNumberGenerationException(String message) {

        super(
                "TICKET_NUMBER_GENERATION_FAILED",
                ErrorType.INTERNAL,
                message
        );
    }
}
