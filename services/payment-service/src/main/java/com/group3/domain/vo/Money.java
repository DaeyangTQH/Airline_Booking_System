package com.group3.domain.vo;

import com.group3.domain.exception.InvalidDomainValueException;

import java.math.BigDecimal;

public record Money(BigDecimal amount, CurrencyCode currency) {
    public Money {
        if (amount == null) {
            throw new InvalidDomainValueException(
                    "Money amount must not be null"
            );
        }

        if (currency == null) {
            throw new InvalidDomainValueException(
                    "Money currency must not be null"
            );
        }

        if (amount.signum() <= 0) {
            throw new InvalidDomainValueException(
                    "Money amount must be greater than 0"
            );
        }

        amount = amount.stripTrailingZeros();
    }

    public Money add(Money other) {
        ensureSameCurrency(other);
        return new Money(amount.add(other.amount()), currency);
    }

    public boolean isGreaterThan(Money other) {
        ensureSameCurrency(other);
        return amount.compareTo(other.amount()) > 0;
    }

    private void ensureSameCurrency(Money other) {
        if (other == null) {
            throw new InvalidDomainValueException(
                    "Money must not be null"
            );
        }

        if (!currency.equals(other.currency())) {
            throw new InvalidDomainValueException(
                    "Cannot operate on Money with different currencies"
            );
        }
    }
}
