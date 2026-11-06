package com.group3.application.integration;

import com.group3.domain.vo.CurrencyCode;
import com.group3.domain.vo.FareClass;

import java.math.BigDecimal;
import java.util.UUID;

public record FlightFareQuote(
        UUID flightId,
        FareClass fareClass,
        BigDecimal unitPrice,
        CurrencyCode currency,
        int availableSeats,
        boolean bookable
) {
}
