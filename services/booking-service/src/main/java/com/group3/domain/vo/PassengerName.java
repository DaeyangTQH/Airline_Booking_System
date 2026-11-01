package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

public record PassengerName(String value) {

    public PassengerName{
        if(value == null || value.isBlank()){
            throw new InvalidDomainValueException(
                    "Passenger name cannot be null or blank"
            );
        }

        value = value.trim();

        if(value.length() > 150) {
            throw new InvalidDomainValueException(
                    "Passenger name must not exceed 150 characters"
            );
        }
    }
}
