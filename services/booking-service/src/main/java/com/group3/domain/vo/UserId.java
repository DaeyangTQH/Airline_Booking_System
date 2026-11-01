package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

import java.util.UUID;

public record UserId(UUID value) {
    public UserId{
        if(value == null) {
            throw new InvalidDomainValueException(
                    "User ID must not be null"
            );
        }
    }
}
