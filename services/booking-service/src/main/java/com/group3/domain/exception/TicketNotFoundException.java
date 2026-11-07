package com.group3.domain.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class TicketNotFoundException extends CustomException {

    public TicketNotFoundException(String message) {

        super(
                "TICKET_NOT_FOUND",
                ErrorType.NOT_FOUND,
                message
        );
    }
}
