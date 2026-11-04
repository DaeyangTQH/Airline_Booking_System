package com.group3.domain.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class RefundNotAllowedException extends CustomException {

    public RefundNotAllowedException(String message) {
        super(
                "REFUND_NOT_ALLOWED",
                ErrorType.CONFLICT,
                message
        );
    }
}
