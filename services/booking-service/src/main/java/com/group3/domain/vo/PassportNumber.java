package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

public record PassportNumber(String value) {

    public PassportNumber{
        if(value == null || value.isBlank()){
            throw new InvalidDomainValueException(
                    "Passport number must not be blank"
            );
        }

        value = value.trim().toUpperCase();

        if(value.length() > 50){
            throw new InvalidDomainValueException(
                    "Passport number must not exceed 50 characters"
            );
        }
    }
}
