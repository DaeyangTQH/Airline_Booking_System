package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

public record TicketNumber(String value) {

    public TicketNumber{
        if(value == null || value.isBlank()){
            throw new InvalidDomainValueException(
                    "Ticket number cannot be null or blank"
            );
        }
        value = value.trim();
    }
}
