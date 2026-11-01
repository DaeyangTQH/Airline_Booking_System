package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

import java.util.UUID;

public record FlightId(UUID value) {
    public FlightId{
        if(value == null){
            throw new InvalidDomainValueException(
                    "Flight ID must not be null"
            );
        }
    }
}
