package com.group3.domain.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class BookingNotFoundException extends CustomException {

    public BookingNotFoundException(String messsage){
        super(
                "BOOKING_NOT_FOUND",
                ErrorType.NOT_FOUND,
                messsage
        );
    }
}
