package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

import java.util.UUID;

public record SeatId(UUID value) {

    public SeatId {
        if (value == null) {
            throw new InvalidDomainValueException(
                    "Seat ID must not be null"
            );
        }
    }
}
