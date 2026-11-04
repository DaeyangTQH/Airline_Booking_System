package com.group3.domain.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class InvalidPaymentStateException extends CustomException {

    public InvalidPaymentStateException(String message) {
        super(
                "INVALID_PAYMENT_STATE",
                ErrorType.CONFLICT,
                message
        );
    }
}
