package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

import java.util.UUID;

public record TicketId(UUID value) {

        public TicketId{
            if(value == null){
                throw new InvalidDomainValueException(
                        "Ticket ID must not be null"
                );
            }
        }

        public static TicketId generate() {
            return new TicketId(UUID.randomUUID());
        }
}
