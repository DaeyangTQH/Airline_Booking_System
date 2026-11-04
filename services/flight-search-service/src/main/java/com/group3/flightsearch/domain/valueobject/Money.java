package com.group3.flightsearch.domain.valueobject;

import com.group3.flightsearch.domain.exception.InvalidDomainValueException;
import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount == null || currency == null) {
            throw new InvalidDomainValueException("Amount and currency are required");
        }
        if (amount.signum() < 0) {
            throw new InvalidDomainValueException("Amount must not be negative");
        }
        amount = amount.stripTrailingZeros();
    }
}
