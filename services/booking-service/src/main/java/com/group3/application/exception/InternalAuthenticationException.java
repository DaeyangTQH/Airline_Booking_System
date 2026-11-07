package com.group3.application.exception;

import com.group3.exception.CustomException;
import com.group3.exception.ErrorType;

public class InternalAuthenticationException extends CustomException {

    public InternalAuthenticationException(String message) {

        super(
                "INVALID_AUTHENTICATION_API_KEY",
                ErrorType.UNAUTHORIZED,
                message
        );
    }
}
