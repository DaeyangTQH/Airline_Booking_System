package com.group3.domain.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class InvalidDomainValueException extends CustomException {

    public InvalidDomainValueException(String message) {
        super(
                "INVALID_DOMAIN_VALUE",
                ErrorType.VALIDATION,
                message
        );
    }
}
