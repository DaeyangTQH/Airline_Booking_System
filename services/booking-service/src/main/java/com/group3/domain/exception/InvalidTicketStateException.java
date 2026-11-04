package com.group3.domain.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class InvalidTicketStateException extends CustomException {

    public InvalidTicketStateException(String message){
        super(
                "INVALID_TICKET_STATE",
                ErrorType.CONFLICT,
                message
        );
    }
}
