package com.group3.exception;

import java.util.Objects;

public class CustomException extends RuntimeException{

    private final String code;
    private final ErrorType type;

    public CustomException(
            String code,
            ErrorType type,
            String message
    ){
        super(message);

        this.code = Objects.requireNonNull(code, "Error code must not be null");

        this.type = Objects.requireNonNull(type, "Error type must not be null");
    }

    public String getCode() {
        return code;
    }

    public ErrorType getType() {
        return type;
    }
}
