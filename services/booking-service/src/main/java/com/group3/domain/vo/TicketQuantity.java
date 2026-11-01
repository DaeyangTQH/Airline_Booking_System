package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

public record TicketQuantity(int value) {
    public TicketQuantity {
        if (value <= 0){
            throw new InvalidDomainValueException(
                    "Ticket quantity must be greater than 0"
            );
        }
    }
}
