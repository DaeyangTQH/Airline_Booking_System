package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

import java.util.UUID;

public record PassengerProfileId(UUID value) {

    public PassengerProfileId{
        if(value == null){
            throw new InvalidDomainValueException(
                    "Passenger Profile ID must not be null"
            );
        }
    }
}
