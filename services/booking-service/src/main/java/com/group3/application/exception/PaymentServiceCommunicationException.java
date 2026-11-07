package com.group3.application.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class PaymentServiceCommunicationException extends CustomException {

    public PaymentServiceCommunicationException(String message) {

        super(
                "PAYMENT_SERVICE_COMMUNICATION_FAILED",
                ErrorType.INTERNAL,
                message
        );
    }
}
