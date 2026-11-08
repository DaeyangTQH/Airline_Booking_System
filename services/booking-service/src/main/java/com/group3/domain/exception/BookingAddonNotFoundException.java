package com.group3.domain.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class BookingAddonBotFoundException extends CustomException {

    public BookingAddonBotFoundException(String message) {

        super(
                "BOOKING_ADDON_NOT_FOUND",
                ErrorType.NOT_FOUND,
                message
        );
    }
}
