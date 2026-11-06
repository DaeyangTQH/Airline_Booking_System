package com.group3.api.dto;

import com.group3.domain.vo.FareClass;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateBookingRequest(

        @NotNull(message = "Flight ID is required")
        UUID flightId,

        @NotNull(message = "Fare class is required")
        FareClass fareClass,

        @Min(
                value = 1,
                message = "Quantity must be greater than 0"
        )
        int quantity,

        @NotNull(message = "Contact information is required")
        @Valid
        BookingContactRequest contact
) {
}
